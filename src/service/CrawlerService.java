package service;

import domain.CrawlerResult;
import domain.Preco;
import domain.Produto;
import domain.port.CrawlerPort;
import infra.adapter.MercadoLivreCrawlerAdapter;
import infra.adapter.NewBalanceCrawlerAdapter;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class CrawlerService {

    private final List<CrawlerPort> crawlers;
    private final ProdutoService produtoService;
    private final PrecoService precoService;

    public CrawlerService(List<CrawlerPort> crawlers, ProdutoService produtoService, PrecoService precoService) {
        this.crawlers = crawlers;
        this.produtoService = produtoService;
        this.precoService = precoService;
    }

    public void monitorar(Produto produto) {
        CrawlerResult menorPreco = null;

        System.out.println("Iniciando crawling nos links do produto: " + produto.getNome());

        for (Map<String, String> link : produto.getLinks()) {
            String loja = link.get("loja");
            String url = link.get("url");
            if (loja == null || url == null || url.isEmpty()) continue;

            // Normaliza o nome da loja para o switch
            String normalizedLoja = loja.toUpperCase().replace(" ", "").replace("_", "").replace("-", "").trim();

            for (CrawlerPort crawler : crawlers) {
                String crawlerTarget = "";
                
                // Escolher crawler por loja utilizando switch
                switch (normalizedLoja) {
                    case "NEWBALANCE":
                        crawlerTarget = "NEW_BALANCE";
                        break;
                    case "MERCADOLIVRE":
                        crawlerTarget = "MERCADO_LIVRE";
                        break;
                    default:
                        // Fallback robusto por domínio caso o usuário tenha cadastrado um nome diferente para a loja
                        if (url.contains("newbalance.com.br")) {
                            crawlerTarget = "NEW_BALANCE";
                        } else if (url.contains("mercadolivre.com.br")) {
                            crawlerTarget = "MERCADO_LIVRE";
                        }
                        break;
                }

                // Executa o crawler correspondente
                switch (crawlerTarget) {
                    case "NEW_BALANCE":
                        if (crawler instanceof NewBalanceCrawlerAdapter) {
                            CrawlerResult result = crawler.extrair(url);
                            if (result.getPreco() != null) {
                                menorPreco = getMenor(menorPreco, result);
                            }
                        }
                        break;
                    case "MERCADO_LIVRE":
                        if (crawler instanceof MercadoLivreCrawlerAdapter) {
                            CrawlerResult result = crawler.extrair(url);
                            if (result.getPreco() != null) {
                                menorPreco = getMenor(menorPreco, result);
                            }
                        }
                        break;
                }
            }
        }

        if (menorPreco != null) {
            // O preço monitorado não precisa ser menor que o banco, basta ser DIFERENTE.
            // Verifica a diferença usando um epsilon pequeno (0.01) para evitar problemas de precisão com Float
            if (produto.getPreco() == null || Math.abs(produto.getPreco() - menorPreco.getPreco()) > 0.01f) {
                if (produto.getPreco() != null) {
                    System.out.println("Preco alterado de " + produto.getPreco() + " para " + menorPreco.getPreco() + " (" + menorPreco.getNomeLoja() + ")");
                } else {
                    System.out.println("Primeiro preco registrado: " + menorPreco.getPreco() + " (" + menorPreco.getNomeLoja() + ")");
                }

                produto.setPreco(menorPreco.getPreco());
                produto.setNomeLoja(menorPreco.getNomeLoja());
                produto.setUrlProduto(menorPreco.getUrlProduto());
                
                // Salva o preço atualizado (com nome de loja e url do produto vencedores) no histórico de preços!
                Preco historico = new Preco(new Date(), menorPreco.getPreco(), produto, menorPreco.getNomeLoja(), menorPreco.getUrlProduto());
                produto.adicionarPreco(historico);
                
                produtoService.edit(produto, produto.getId());
            } else {
                System.out.println("Preco mantido: " + menorPreco.getPreco() + " (" + menorPreco.getNomeLoja() + ") - Já é o preço atual no banco.");
            }
        } else {
            System.out.println("Nenhum preco foi extraido das URLs fornecidas.");
        }
    }

    private CrawlerResult getMenor(CrawlerResult c1, CrawlerResult c2) {
        if (c1 == null) return c2;
        if (c2 == null) return c1;
        return c1.getPreco() < c2.getPreco() ? c1 : c2;
    }
}
