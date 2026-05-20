import domain.Preco;
import domain.Produto;
import infra.HibernateUtil;
import domain.port.CrawlerPort;
import infra.adapter.MercadoLivreCrawlerAdapter;
import infra.adapter.NewBalanceCrawlerAdapter;
import service.CrawlerService;
import service.PrecoService;
import service.ProdutoService;
import service.ServiceInterface;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

void main() {
    ProdutoService produtoService = new ProdutoService();
    PrecoService precoService = new PrecoService();

    try {
        boolean menuAtivo = true;
        while (menuAtivo) {
            int opcaoSelecionada = menu();
            switch (opcaoSelecionada) {
                case 1:
                    adicionarProduto(produtoService, precoService);
                    break;
                case 2:
                    listarProdutos(produtoService);
                    break;
                case 3:
                    editarProdutos(produtoService, precoService);
                    break;
                case 4:
                    deletarProdutos(produtoService);
                    break;
                case 5:
                    editarPreco(produtoService, precoService);
                    break;
                case 6:
                    listarPrecos(precoService);
                    break;
                case 7:
                    executarCrawler(produtoService, precoService);
                    break;
                case 0:
                    menuAtivo = false;
                    break;
            }
        }
    } finally {
        HibernateUtil.shutdown();
    }
}

public void adicionarLinks(Produto produto) {
    boolean adicionarMais = true;
    while (adicionarMais) {
        String resp = IO.readln("Deseja adicionar um link de monitoramento? (S/N): ");
        if (resp.equalsIgnoreCase("S")) {
            String loja = IO.readln("Digite a loja (ex: NEW_BALANCE, MERCADO_LIVRE): ");
            String url = IO.readln("Digite a URL correspondente: ");
            Map<String, String> link = new HashMap<>();
            link.put("loja", loja.toUpperCase().trim());
            link.put("url", url.trim());
            produto.getLinks().add(link);
        } else {
            adicionarMais = false;
        }
    }
}

public void adicionarProduto(ProdutoService produtoService, PrecoService precoService) {
    String sku = IO.readln("Digite a SKU do produto: ");
    String nome = IO.readln("Digite o nome do produto: ");
    String marca = IO.readln("Digite a marca do produto: ");
    String descricao = IO.readln("Digite a descricao do produto: ");
    Float preco = Float.parseFloat(IO.readln("Digite o preco do produto: "));

    Produto produto = new Produto(sku, nome, marca, descricao, preco);
    adicionarLinks(produto);
    produtoService.add(produto);

    Date dataAtual = new Date();
    Preco novoPreco = new Preco(dataAtual, preco, produto, produto.getNomeLoja(), produto.getUrlProduto());
    precoService.add(novoPreco);
}

public void listarProdutos(ProdutoService service) {
    service.list();
}

public void editarProdutos(ProdutoService produtoService, PrecoService precoService) {
    System.out.println("Atualmente temos os seguintes produtos cadastrados: ");
    produtoService.list();
    int indice = Integer.parseInt(IO.readln("Digite o indice do produto que deseja editar: "));

    Produto produto = (Produto) produtoService.findByIndex(indice);
    produto.setSku(IO.readln("Informe o novo SKU do produto: "));
    produto.setNome(IO.readln("Informe o novo nome do produto: "));
    produto.setDescricao(IO.readln("Informe a nova descricao do produto: "));
    produto.setMarca(IO.readln("Informe a nova marca do produto: "));
    produto.setPreco(Float.parseFloat(IO.readln("Informe o novo preco do produto: ")));

    String alterarLinks = IO.readln("Deseja reconfigurar os links de monitoramento? (S/N): ");
    if (alterarLinks.equalsIgnoreCase("S")) {
        produto.getLinks().clear();
        adicionarLinks(produto);
    }

    produtoService.edit(produto, produto.getId());

    Date dataAtual = new Date();
    Preco novoPreco = new Preco(dataAtual, produto.getPreco(), produto, produto.getNomeLoja(), produto.getUrlProduto());
    precoService.add(novoPreco);
}

public void deletarProdutos(ProdutoService service) {
    System.out.println("Atualmente temos os seguintes produtos cadastrados: ");
    service.list();
    int indice = Integer.parseInt(IO.readln("Digite o indice do produto que deseja deletar: "));
    Produto produto = (Produto) service.findByIndex(indice);
    service.remove(produto);
}

public void editarPreco(ProdutoService produtoService, PrecoService precoService) {
    produtoService.list();
    int indice = Integer.parseInt(IO.readln("Digite o indice do produto que deseja alterar: "));

    Produto produto = (Produto) produtoService.findByIndex(indice);
    produto.setPreco(Float.parseFloat(IO.readln("Digite o novo preço: ")));
    produtoService.edit(produto, produto.getId());

    Date dataAtual = new Date();
    Preco novoPreco = new Preco(dataAtual, produto.getPreco(), produto, produto.getNomeLoja(), produto.getUrlProduto());
    precoService.add(novoPreco);
}

public void listarPrecos(PrecoService precoService){
    precoService.list();
}

public void executarCrawler(ProdutoService produtoService, PrecoService precoService) {
    CrawlerPort nbCrawler = new NewBalanceCrawlerAdapter();
    CrawlerPort mlCrawler = new MercadoLivreCrawlerAdapter();
    
    CrawlerService crawlerService = new CrawlerService(Arrays.asList(nbCrawler, mlCrawler), produtoService, precoService);
    
    List<Produto> produtos = produtoService.listar();
    if (produtos.isEmpty()) {
        System.out.println("Nenhum produto cadastrado no banco de dados para monitoramento.");
        return;
    }
    
    for (Produto p : produtos) {
        crawlerService.monitorar(p);
    }
}

public Integer menu() {
    System.out.println("Digite a opção desejada: ");
    System.out.println("1 = Adicionar um novo produto");
    System.out.println("2 = Listar os produtos");
    System.out.println("3 = Editar um produto");
    System.out.println("4 = Deletar um produto");
    IO.println("5 = Editar um preço");
    IO.println("6 = Listar o Histórico de Preços");
    IO.println("7 = Executar Monitoramento / Crawler");
    System.out.println("0 = Sair");

    int opcao = Integer.parseInt(IO.readln());
    return opcao;
}
