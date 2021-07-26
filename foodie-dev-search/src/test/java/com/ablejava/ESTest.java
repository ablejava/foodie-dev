package com.ablejava;

import com.ablejava.pojo.Stu;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.GetQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

/**
 * @Author: xiazhongwei
 * @Date: 2021/7/26 21:32
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ESTest {

    @Autowired
    private ElasticsearchTemplate es;

    /**
     * 通过template 创建索引的shards 和replicas 不起作用
     * short shards() default 5;
     * short replicas() default 1;
     */
    @Test
    public void createIndexStu() {

        Stu stu = new Stu();
        IndexQuery indexQuery = new IndexQueryBuilder().withObject(stu).build();
        es.index(indexQuery);
    }

    @Test
    public void update() {
        // source map 定义要修改的字段和修改的value值
        Map<String, Object> sourceMap = Maps.newHashMap();
        sourceMap.put("sign", "im not a sign");
        IndexRequest indexRequest = new IndexRequest();
        indexRequest.source(sourceMap);
        UpdateQuery build = new UpdateQueryBuilder().withClass(Stu.class)
                .withId("1002").withIndexRequest(indexRequest).build();
        es.update(build);
    }

    @Test
    public void getQuery() {
        GetQuery getQuery = new GetQuery();
        getQuery.setId("1002");
        Stu stu = es.queryForObject(getQuery, Stu.class);

    }

    @Test
    public void delete(){

        String delete = es.delete(Stu.class, "1002");

    }

    @Test
    public void search() {
        SearchQuery builder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("age", 29))
                .withHighlightFields
                        (new HighlightBuilder.Field("desc")
                                .preTags("<font color='red'>")
                                .postTags("</font>")
                        )
                .withSort(new FieldSortBuilder("age").order(SortOrder.ASC))
                .withPageable(PageRequest.of(0, 10)).build();
        AggregatedPage<Stu> stus = es.queryForPage(builder, Stu.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {

                List<Stu> stuList = Lists.newArrayList();

                SearchHits hits = searchResponse.getHits();
                for (SearchHit hit : hits) {

                    Object id = hit.getSourceAsMap().get("id"); // 直接强制转换为Long 会报错
                    String name = (String) hit.getSourceAsMap().get("name");
                    String desc = "desc";
                    HighlightField highlightField = hit.getHighlightFields().get(desc);
                    String descStr = highlightField.getFragments()[0].toString();

                    Stu stuHl = new Stu();
                    stuHl.setDesc(descStr);
                    stuHl.setStuId(Long.valueOf(id.toString()));
                    stuHl.setName(name);
                    stuList.add(stuHl);
                }

                if (stuList.size() >0) {
                    return new AggregatedPageImpl<>((List<T>)stuList);
                }
                return null;
            }
        });
        int totalPages = stus.getTotalPages();
        List<Stu> content = stus.getContent();
    }
}
