package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand> {

    @Insert("INSERT INTO `tb_category_brand` (category_id,brand_id) VALUES(#{cid},#{bid})")
    int insertCategoryBrand(@Param("cid")Long cid,@Param("bid")Long bid);

    @Select("SELECT b.id,b.name,b.letter,b.image FROM tb_brand b,tb_category_brand cb WHERE b.id=cb.brand_id and cb.category_id=#{cid}")
    List<Brand> queryBrandByCid(@Param("cid")Long cid);
}
