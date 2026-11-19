
package org.cdpg.dx.database.elastic.service;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Field;
import org.cdpg.dx.database.elastic.ElasticClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Regression test: ElasticsearchVerticle can be deployed twice in one JVM (local + central
 * catalogue). Each ElasticsearchServiceImpl must keep its own client rather than sharing one via
 * static state.
 */
class ElasticsearchServiceImplTest {

  private ElasticClient localClient;
  private ElasticClient centralClient;

  @AfterEach
  void tearDown() throws Exception {
    if (localClient != null) localClient.close();
    if (centralClient != null) centralClient.close();
  }

  @Test
  void eachInstanceKeepsItsOwnClient() throws Exception {
    localClient = new ElasticClient("local-es", 9200, "user", "pass");
    centralClient = new ElasticClient("central-es", 80, "user", "pass");

    ElasticsearchServiceImpl local = new ElasticsearchServiceImpl(localClient);
    ElasticsearchServiceImpl central = new ElasticsearchServiceImpl(centralClient);

    assertSame(localClient, read(local, "client"));
    assertSame(localClient.getClient(), read(local, "asyncClient"));
    assertSame(centralClient, read(central, "client"));
    assertSame(centralClient.getClient(), read(central, "asyncClient"));
    assertNotSame(read(local, "asyncClient"), read(central, "asyncClient"));
  }

  private static Object read(ElasticsearchServiceImpl impl, String name) throws Exception {
    Field f = ElasticsearchServiceImpl.class.getDeclaredField(name);
    f.setAccessible(true);
    return f.get(impl);
  }
}
