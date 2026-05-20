package domain;

public class CrawlerResult {
    private Float preco;
    private String nomeLoja;
    private String urlProduto;

    public CrawlerResult() {
    }

    public CrawlerResult(Float preco, String nomeLoja, String urlProduto) {
        this.preco = preco;
        this.nomeLoja = nomeLoja;
        this.urlProduto = urlProduto;
    }

    public Float getPreco() {
        return preco;
    }

    public void setPreco(Float preco) {
        this.preco = preco;
    }

    public String getNomeLoja() {
        return nomeLoja;
    }

    public void setNomeLoja(String nomeLoja) {
        this.nomeLoja = nomeLoja;
    }

    public String getUrlProduto() {
        return urlProduto;
    }

    public void setUrlProduto(String urlProduto) {
        this.urlProduto = urlProduto;
    }
}
