package com.leyou.item.api;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsApi {
    @GetMapping("sku/list")
    List<Sku> querySkuListBySpuId(@RequestParam("id")Long id);

    @GetMapping("spu/page")
    PageResult<Spu>querySpuByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam(value = "key",required = false)String key
    );

    @GetMapping("spu/detail/{spuId}")
    SpuDetail querySpuDetailBySpuId(@PathVariable("spuId")Long spuId);

    @GetMapping("spu/{id}")
    Spu querySpuById(@PathVariable("id")Long id);

    @GetMapping("sku/list/ids")
    List<Sku>querySkuListById(@RequestParam("ids")List<Long>ids);

    @PostMapping("stock/decrease")
     void stockDecrease(@RequestBody List<CartDTO> cartDTOS);
}
