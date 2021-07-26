package com.ablejava.foodiedev.service;

import com.ablejava.foodiedev.pojo.Category;
import com.ablejava.foodiedev.pojo.vo.CategoryVO;
import com.ablejava.foodiedev.pojo.vo.NewItemsVo;

import java.util.List;

/**
 * @author mrcode
 * @date 2021/2/13 22:10
 */
public interface CategoryService {
    /**
     * 获取所有一级分类
     *
     * @return
     */
    List<Category> queryAllRootLeveCat();

    /**
     * 获取所有子级分类，按层级返回
     *
     * @param rootCatId
     * @return
     */
    List<CategoryVO> getSubCatList(Integer rootCatId);

    /**
     * 查询首页每个一级分类下的 6 条最新商品数据
     *
     * @param rootCatId
     * @return
     */
    List<NewItemsVo> getSixNewItemsLazy(Integer rootCatId);
}
