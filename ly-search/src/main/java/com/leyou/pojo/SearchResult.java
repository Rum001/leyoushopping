package com.leyou.pojo;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author: HuYi.Zhang
 * @create: 2018-07-01 16:11
 **/
@Data
public class SearchResult extends PageResult<Goods> {

    private List<Category> categories;

    private List<Brand> brands;

    private List<Map<String,Object>>specs;

    public SearchResult() {
    }
    public SearchResult(Long total, Long totalPage, List<Goods> items,
                        List<Category> categories, List<Brand> brands,List<Map<String,Object>>specs) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
        this.specs=specs;
    }

}
