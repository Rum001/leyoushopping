package com.leyou.repository;

import com.leyou.client.GoodsClient;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.pojo.Goods;
import com.leyou.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;

    @Test
    public void createIndex(){
        //创建索引库
        elasticsearchTemplate.createIndex(Goods.class);
        //映射关系
        elasticsearchTemplate.putMapping(Goods.class);
    }

    @Test
    public void loadData(){
        int page=1;
        int row=100;
        int size=0;
        //查询spu
        do {
            PageResult<Spu> spuPageResult = goodsClient.querySpuByPage(page, row, true, null);
            List<Spu> spus = spuPageResult.getItems();
            if(CollectionUtils.isEmpty(spus)){
                break;
            }
            List<Goods> goods = spus.stream().map(searchService::buildGoods).collect(Collectors.toList());
            goodsRepository.saveAll(goods);
            page++;
            size=spus.size();
        }while (size==100);

    }
}
