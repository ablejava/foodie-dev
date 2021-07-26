package com.ablejava.foodiedev.api.controller;

import com.ablejava.foodiedev.common.enums.YesOrNo;
import com.ablejava.foodiedev.common.util.JSONResult;
import com.ablejava.foodiedev.pojo.Carousel;
import com.ablejava.foodiedev.pojo.Category;
import com.ablejava.foodiedev.pojo.vo.CategoryVO;
import com.ablejava.foodiedev.pojo.vo.NewItemsVo;
import com.ablejava.foodiedev.service.CarouselService;
import com.ablejava.foodiedev.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author mrcode
 * @date 2021/2/13 21:44
 */
@Api(value = "首页", tags = {"首页相关接口"})
@RestController
@RequestMapping("/index")
public class IndexController {
    @Autowired
    private CarouselService carouselService;
    @Autowired
    private CategoryService categoryService;

    @ApiOperation(value = "获取首页轮播图列表")
    @GetMapping("/carousel")
    public JSONResult carousel() {
        List<Carousel> carousels = carouselService.queryAll(YesOrNo.YES.type);
        return JSONResult.ok(carousels);
    }

    @ApiOperation(value = "获取商品一级分类")
    @GetMapping("/cats")
    public JSONResult cats() {
        List<Category> list = categoryService.queryAllRootLeveCat();
        return JSONResult.ok(list);
    }

    @ApiOperation(value = "获取商品子级分类")
    @GetMapping("/subCat/{rootCatId}")
    public JSONResult subCat(
            @ApiParam(name = "rootCatId", value = "一级分类 ID", required = true)
            @PathVariable Integer rootCatId) {
        List<CategoryVO> list = categoryService.getSubCatList(rootCatId);
        return JSONResult.ok(list);
    }

    @ApiOperation(value = "获取一级分类下的最新 6 个商品")
    @GetMapping("/sixNewItems/{rootCatId}")
    public JSONResult sixNewItems(
            @ApiParam(name = "rootCatId", value = "一级分类 ID", required = true)
            @PathVariable Integer rootCatId) {
        List<NewItemsVo> list = categoryService.getSixNewItemsLazy(rootCatId);
        return JSONResult.ok(list);
    }
}
