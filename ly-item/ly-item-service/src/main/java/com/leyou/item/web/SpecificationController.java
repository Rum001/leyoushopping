package com.leyou.item.web;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    @Autowired
    private BrandService brandService;

    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>>querySpecGroupByCid(@PathVariable("cid")Long cid){
        return ResponseEntity.ok(specificationService.querySpecGroupByCid(cid));
    }

//    @GetMapping("params")
//    public ResponseEntity<List<SpecParam>>querySpecParamByGid(@RequestParam("gid")Long gid){
//        return ResponseEntity.ok(specificationService.querySpecParamByList(gid,null,null));
//    }

    @GetMapping("params")
    public ResponseEntity<List<SpecParam>>querySpecParamByList(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "searching",required = false)Boolean searching
            ){
        return ResponseEntity.ok(specificationService.querySpecParamByList(gid,cid,searching));
    }

    /**
     * 根据Cid查询出所有规格参数的组和参数
     * @param cid
     * @return
     */
    @GetMapping("group")
    public ResponseEntity<List<SpecGroup>>queryGroupByCid(@RequestParam("cid")Long cid){
        return ResponseEntity.ok(specificationService.queryGroupByCid(cid));
    }
    @PostMapping("group")
    public ResponseEntity<Void>addGroup(@RequestBody SpecGroup specGroup){
        specificationService.addGroup(specGroup);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PostMapping("param")
    public ResponseEntity<Void>addSpecParam(@RequestBody SpecParam specParam) {
        specificationService.addSpecParam(specParam);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PutMapping("param")
    public ResponseEntity<Void>updateSpecParam(@RequestBody SpecParam specParam){
        specificationService.updateSepcParam(specParam);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @DeleteMapping("param/{id}")
    public ResponseEntity<Void>deleteSpecParam(@PathVariable("id")Long id){
        specificationService.deleteSpecParam(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @DeleteMapping("group/{id}")
    public ResponseEntity<Void>deleteSpecGroup(@PathVariable("id")Long id){
        specificationService.deleteSpecGroup(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


}
