package com.leyou.item.web;

import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @GetMapping("list")
    @ResponseBody
    public ResponseEntity<List<Category>>queryCategoryByPid(@RequestParam Long pid){
        return ResponseEntity.ok(categoryService.queryCategoryByPid(pid));
    }

    @GetMapping("list/ids")
    public ResponseEntity<List<Category>>queryCategoryByIds(@RequestParam("ids")List<Long>ids){
        return ResponseEntity.ok(categoryService.queryByIds(ids));
    }
    @GetMapping("bid/{id}")
    public ResponseEntity<Brand>queryBrandById(@PathVariable("id")Long id){
        return ResponseEntity.ok(brandService.queryById(id));
    }
}
