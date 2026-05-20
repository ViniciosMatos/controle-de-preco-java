package domain.port;

import domain.CrawlerResult;

public interface CrawlerPort {
    CrawlerResult extrair(String url);
}
