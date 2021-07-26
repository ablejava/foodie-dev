package com.ablejava.foodiedev.service;

import com.ablejava.foodiedev.pojo.Carousel;

import java.util.List;

/**
 * 轮播图
 *
 * @author mrcode
 * @date 2021/2/13 21:40
 */
public interface CarouselService {
    List<Carousel> queryAll(Integer isShow);
}
