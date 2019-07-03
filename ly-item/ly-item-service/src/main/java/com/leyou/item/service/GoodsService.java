package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {

        //分页
        PageHelper.startPage(page,rows);

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //过滤
        if(StringUtils.isNotBlank(key)){
           criteria.andLike("title","%"+key+"%");
        }
        //是否上下架
        if(saleable!=null){
            criteria.andEqualTo("saleable",saleable);
        }
        //默认排序
        example.setOrderByClause("last_update_time DESC");
        //查询
        List<Spu> spus = spuMapper.selectByExample(example);

        loadCategoryAndBrand(spus);

        //解析获得总条数
        PageInfo<Spu> info = new PageInfo<>(spus);

        return new PageResult(info.getTotal(),spus);

    }

    private void loadCategoryAndBrand(List<Spu> spus) {
        for (Spu spu : spus) {
            //处理分类名称
            List<String> list = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3())).stream().
                    map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(list,"/"));
            //处理品牌名称
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }

    }

    @Transactional
    public void saveGoods(Spu spu) {
        //新增spu
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);
        int count = spuMapper.insert(spu);
        if(count!=1){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

        //新增detail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        count = spuDetailMapper.insert(spuDetail);
        if(count!=1){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        List<Stock>stockList=new ArrayList<>();
        //新增sku
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());
            count = skuMapper.insert(sku);
            if(count!=1){
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            //新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockList.add(stock);
        }
         stockMapper.insertList(stockList);

        //发送mq
        try {
            amqpTemplate.convertAndSend("item.insert",spu.getId());
        }catch (Exception e){
            log.error("[消息发送异常]",e);
        }
    }

    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if(spuDetail==null){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        return spuDetail;
    }

    public List<Sku> querySkuListBySpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);
        //查询库存
        List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
        loadStoke(ids, skuList);
        return skuList;
    }

    public Spu querySpuById(Long id) {
        //查询SPU
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu==null){
            throw  new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //查询SKU
        spu.setSkus(querySkuListBySpuId(id));
        //查询SPU详情
        spu.setSpuDetail(querySpuDetailBySpuId(id));
        return spu;
    }

    public List<Sku> querySkuListById(List<Long> ids) {
        //根据ID查询sku的集合
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //查询出这个sku集合下的库存并重新赋值
        loadStoke(ids, skus);
        return skus;
    }

    private void loadStoke(List<Long> ids, List<Sku> skus) {
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        Map<Long, Integer> map = stockList.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skus.forEach(sku -> sku.setStock(map.get(sku.getId())));
    }

    @Transactional
    public void stockDecrease(List<CartDTO> cartDTOS) {
        for (CartDTO cartDTO : cartDTOS) {
            int count = stockMapper.stockDecrease(cartDTO.getSkuId(), cartDTO.getNum());
            if (count!=1){
                throw new LyException(ExceptionEnum.STOCK_UPDATE_ERROR);
            }
        }
    }
}
