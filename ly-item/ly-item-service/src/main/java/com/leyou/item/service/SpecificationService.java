package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> querySpecGroupByCid(Long cid) {
        SpecGroup t = new SpecGroup();
        t.setCid(cid);
        List<SpecGroup> list = specGroupMapper.select(t);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return list;
    }
    public List<SpecParam> querySpecParamByList(Long gid,Long cid,Boolean searching) {
        SpecParam param = new SpecParam();
        param.setGroupId(gid);
        param.setCid(cid);
        param.setSearching(searching);
        List<SpecParam> list = specParamMapper.select(param);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        return list;
    }

    public List<SpecGroup> queryGroupByCid(Long cid) {
        //查询出所有的规格组
        List<SpecGroup> specGroups = querySpecGroupByCid(cid);
        //查询出所有的规格参数
        List<SpecParam> specParams = querySpecParamByList(null, cid, null);

        //把 规格参数变为Map KEY为 组ID  值为 规格参数
        Map<Long,List<SpecParam>>map=new HashMap<>();
        for (SpecParam specParam : specParams) {
            //判断这个map中是否包含key 如何不包含则 新增一个list
            if (!map.containsKey(specParam.getGroupId())){
                map.put(specParam.getGroupId(),new ArrayList<>());
            }
            //存在则把它添加到Map中
            map.get(specParam.getGroupId()).add(specParam);
        }

        //把规格参数放到 规格组里
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }
        return specGroups;
    }

    public void addGroup(SpecGroup specGroup) {
        int count = specGroupMapper.insert(specGroup);
        if (count!=1){
            throw new LyException(ExceptionEnum.SPEC_GROUP_SAVE_ERROR);
        }
    }

    public void addSpecParam(SpecParam specParam) {
        int count = specParamMapper.insert(specParam);
        if (count!=1){
            throw new LyException(ExceptionEnum.SPEC_GROUP_SAVE_ERROR);
        }
    }

    public void updateSepcParam(SpecParam specParam) {
        int count = specParamMapper.updateByPrimaryKey(specParam);
        if (count!=1){
            throw new LyException(ExceptionEnum.SPEC_PRARM_UPDATE_ERROR);
        }
    }

    public void deleteSpecParam(Long id) {
        int count = specParamMapper.deleteByPrimaryKey(id);
        if (count!=1){
            throw new LyException(ExceptionEnum.SPEC_PRARM_DELETE_ERROR);
        }

    }

    public void deleteSpecGroup(Long id) {
        int count =specGroupMapper.deleteByPrimaryKey(id);
        if (count!=1){
            throw new LyException(ExceptionEnum.SPEC_PRARM_DELETE_ERROR);
        }
    }
}
