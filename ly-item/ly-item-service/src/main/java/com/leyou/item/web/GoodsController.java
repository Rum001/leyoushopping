package com.leyou.item.web;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("spu/page")
    public ResponseEntity<PageResult<Spu>>querySpuByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam(value = "key",required = false)String key
    ){
        return ResponseEntity.ok(goodsService.querySpuByPage(page,rows,saleable,key));
    }

    @PostMapping("goods")
    public ResponseEntity<Void>saveGoods(@RequestBody Spu spu){
        goodsService.saveGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail>querySpuDetailBySpuId(@PathVariable("spuId")Long spuId){
        return ResponseEntity.ok(goodsService.querySpuDetailBySpuId(spuId));
    }

    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>>querySkuListBySpuId(@RequestParam("id")Long id){
        return ResponseEntity.ok(goodsService.querySkuListBySpuId(id));
    }

    @GetMapping("spu/{id}")
    public ResponseEntity<Spu>querySpuById(@PathVariable("id")Long id){
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }

    @GetMapping("sku/list/ids")
    public ResponseEntity<List<Sku>>querySkuListById(@RequestParam("ids")List<Long>ids){
        return ResponseEntity.ok(goodsService.querySkuListById(ids));
    }

    @PostMapping("stock/decrease")
    public ResponseEntity<Void>stockDecrease(@RequestBody List<CartDTO> cartDTOS){
        goodsService.stockDecrease(cartDTOS);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
