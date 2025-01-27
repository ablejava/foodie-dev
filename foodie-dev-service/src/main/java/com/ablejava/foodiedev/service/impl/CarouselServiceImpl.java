package com.ablejava.foodiedev.service.impl;

import com.ablejava.foodiedev.mapper.CarouselMapper;
import com.ablejava.foodiedev.pojo.Carousel;
import com.ablejava.foodiedev.service.CarouselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author mrcode
 * @date 2021/2/13 21:41
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
public class CarouselServiceImpl implements CarouselService {
    @Autowired
    private CarouselMapper carouselMapper;

    @Override
    public List<Carousel> queryAll(Integer isShow) {
        Example example = new Example(Carousel.class);
        example.orderBy("sort").desc();
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("isShow", isShow);
        List<Carousel> carousels = carouselMapper.selectByExample(example);
        return carousels;
    }
}
