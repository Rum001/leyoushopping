package com.leyou.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.client.BrandClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.client.SpecificationClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.pojo.Goods;
import com.leyou.pojo.SearchRequest;
import com.leyou.pojo.SearchResult;
import com.leyou.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.leyou.common.utils.NumberUtils;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    public Goods buildGoods(Spu spu) {
        Long spuId = spu.getId();
        Goods goods = new Goods();
        //查询SKU
        List<Sku> skusList = goodsClient.querySkuListBySpuId(spuId);
//        List<Long> priceList = skusList.stream().map(Sku::getPrice).collect(Collectors.toList());
        List<Long> priceList = new ArrayList<>();
        //对SKU进行处理
        List<Map<String,Object>>skusMap=new ArrayList<>();
        for (Sku sku : skusList) {
            Map<String,Object>map=new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            map.put("image",StringUtils.substringBefore(sku.getImages(),","));
            skusMap.add(map);
            priceList.add(sku.getPrice());
        }
        //查询分类名字
        List<Category> categories = categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        Stream<String> names = categories.stream().map(Category::getName);
        //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());

        String all=spu.getTitle()+StringUtils.join(names," ")+brand.getName();

        Map<String,Object>specs=new HashMap<>();
        //查询规格参数
        List<SpecParam> params = specificationClient.querySpecParamByList(null, spu.getCid3(), true);
        if(CollectionUtils.isEmpty(params)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        //查询商品详情
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spuId);
        //获取通用规格参数
        Map<String, String> genericSpec = JsonUtils.parseMap(spuDetail.getGenericSpec(), String.class, String.class);
        //获取特有规格参数
        Map<String, List<Object>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<String, List<Object>>>() {
        });
        for (SpecParam param : params) {
            String key = param.getName();
            Object value=null;
            //是否是通用规格参数
            if(param.getGeneric()){
                value=genericSpec.get(param.getId().toString());
                //是否是数字类型
                if (param.getNumeric()){
                    value=chooseSegment(value.toString(),param);
                }
            }else{
                value=specialSpec.get(param.getId().toString());
            }
            specs.put(key,value);
        }
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setBrandId(spu.getBrandId());
        goods.setPrice(priceList);// 价格
        goods.setSkus(JsonUtils.serialize(skusMap));//  jhm  SKU
        goods.setAll(all);// 查询的 标题 品牌  分类
        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());
        goods.setSpecs(specs);// 规格
        return goods;
    }
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }
    public SearchResult search(SearchRequest request) {
    String key = request.getKey();
        if (StringUtils.isBlank(key)) {
        // 如果用户没搜索条件，我们可以给默认的，或者返回null
        return null;
    }

    Integer page = request.getPage() - 1;// page 从0开始
    Integer size = request.getSize();

    // 1、创建查询构建器
    NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
    // 2、查询
    // 2.1、对结果进行筛选
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "skus", "subTitle"}, null));
    // 2.2、基本查询
    QueryBuilder basicQuery = buildBasicQuery(request);
        queryBuilder.withQuery(basicQuery);

    // 2.3、分页
        queryBuilder.withPageable(PageRequest.of(page, size));

    // 2.4、聚合
    String categoryAggName = "categoryAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
    String brandAggName = "brandAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

    // 3、返回结果
    AggregatedPage<Goods> result = (AggregatedPage<Goods>) this.goodsRepository.search(queryBuilder.build());

    // 4、解析结果
    // 4.1.普通分页结果
    long total = result.getTotalElements();
    long totalPage = (total + size - 1) / size;

    // 4.2、解析聚合结果

    // 解析分类结果
    List<Category> categories = parseCategory(result.getAggregation(categoryAggName));
    // 解析品牌的结果
    List<Brand> brands = parseBrand(result.getAggregation(brandAggName));

    // 5、判断是否需要对规格进行聚合
    List<Map<String, Object>> specs = null;
        if (categories.size() == 1) {
        specs = getSpecAgg(categories.get(0).getId(), basicQuery);
    }

        return new SearchResult(total, totalPage, result.getContent(), categories, brands, specs);
}

    private QueryBuilder buildBasicQuery(SearchRequest request) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 基本查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()));
        // 过滤
        BoolQueryBuilder filterBuilder = QueryBuilders.boolQuery();
        // 过滤条件
        Map<String, String> filters = request.getFilter();
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String key = entry.getKey();
            if(!"cid3".equals(key) && !"brandId".equals(key)){
                key = "specs." + key + ".keyword";
            }
            filterBuilder.must(QueryBuilders.termQuery(key,entry.getValue()));
        }

        queryBuilder.filter(filterBuilder);
        return queryBuilder;
    }

    private List<Map<String, Object>> getSpecAgg(Long cid, QueryBuilder basicQuery) {
        // 1、根据分类查找可搜索的规格参数
        List<SpecParam> params = this.specificationClient.querySpecParamByList(
                null, cid,  true);
        // 2、对规格参数聚合
        // 2.1、创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withPageable(PageRequest.of(0,1));
        // 2.2、添加过滤条件
        queryBuilder.withQuery(basicQuery);
        // 2.3、添加聚合
        for (SpecParam param : params) {
            String name = param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name + ".keyword"));
        }

        // 3、查询
        Map<String, Aggregation> aggMap = this.elasticsearchTemplate.query(queryBuilder.build(),
                response -> response.getAggregations()).asMap();

        // 4、解析聚合结果
        List<Map<String, Object>> specs = new ArrayList<>();
        for (SpecParam param : params) {
            // 根据参数名取聚合结果
            StringTerms terms = (StringTerms) aggMap.get(param.getName());
            Map<String,Object> map = new HashMap<>();
            map.put("k", param.getName());
            map.put("options", terms.getBuckets().stream().map(b -> b.getKeyAsString()));
            specs.add(map);
        }
        return specs;
    }

    private List<Brand> parseBrand(Aggregation agg) {
        LongTerms terms = (LongTerms) agg;
        // 解析商品分类
        List<Long> ids = terms.getBuckets().stream()
                .map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
        // 根据品牌id查询所有的品牌
        return this.brandClient.queryBrandByIds(ids);
    }

    private List<Category> parseCategory(Aggregation agg) {
        LongTerms terms = (LongTerms) agg;
        List<Long> ids = terms.getBuckets().stream().map(c -> c.getKeyAsNumber().longValue()).collect(Collectors.toList());
        List<Category> categoryList = categoryClient.queryNameByIds(ids);
        return categoryList;
    }

    public void createdInsertIndex(Long spuId) {
        //查询出spu
        Spu spu = goodsClient.querySpuById(spuId);
        //构建goods对象
        Goods goods = buildGoods(spu);
        //放入索引库
        goodsRepository.save(goods);
    }

    public void deleteInsertIndex(Long spuId) {
        goodsRepository.deleteById(spuId);
    }
}
