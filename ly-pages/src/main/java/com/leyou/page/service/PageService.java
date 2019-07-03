package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PageService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private TemplateEngine templateEngine;

    public Map<String,Object> loadModel(Long id) {
        Map<String,Object>map=new HashMap<>();
        Spu spu = goodsClient.querySpuById(id);
        List<Sku> skus = spu.getSkus();
        SpuDetail detail = spu.getSpuDetail();
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        List<Category> categories = categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        List<SpecGroup> specs = specificationClient.queryGroupByCid(spu.getCid3());
        //添加数据
        map.put("spu", spu);
        map.put("detail", detail);
        map.put("skus", skus);
        map.put("categories", categories);
        map.put("brand", brand);
        map.put("specGroups", specs);
        return map;
    }

    public void createdHtml(Long spuId){
        //获取上下文
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        //输出流
        File file = new File("F:"+File.separator+"upload",spuId+".html");

        //如果文件存在则删除
        if (file.exists()){
            file.delete();
        }
        //将输入流打印输出
        try(PrintWriter writer=new PrintWriter(file,"UTF-8");){
            templateEngine.process("item",context,writer);
        }catch (Exception e){
            log.error("[静态页面生成失败]");
        }
    }

    public void deleteInsertIndex(Long spuId) {
        File file = new File("F:"+File.separator+"upload",spuId+".html");
        if (file.exists()){
            file.delete();
        }
    }
}
