package infra.adapter;

import domain.CrawlerResult;
import domain.port.CrawlerPort;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Map;

public class NewBalanceCrawlerAdapter implements CrawlerPort {

    @Override
    public CrawlerResult extrair(String url) {
        System.out.println("Crawling New Balance: " + url);
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .referrer("https://www.google.com")
                    .timeout(10000)
                    .get();

            Float price = null;

            // Método 1: JSON-LD (Altamente robusto, comum em VTEX)
            Elements jsonLdScripts = doc.select("script[type=application/ld+json]");
            for (Element script : jsonLdScripts) {
                try {
                    String jsonText = script.html().trim();
                    JsonElement jsonElement = JsonParser.parseString(jsonText);
                    price = findPriceInJson(jsonElement);
                    if (price != null) {
                        break;
                    }
                } catch (Exception e) {
                    // Ignora erros de parsing de JSON específico
                }
            }

            // Método 2: Meta tags
            if (price == null) {
                Element metaPrice = doc.selectFirst("meta[property=product:price:amount], meta[itemprop=price], meta[property=og:price:amount]");
                if (metaPrice != null) {
                    price = parsePriceString(metaPrice.attr("content"));
                }
            }

            // Método 3: Seletores VTEX / New Balance comuns
            if (price == null) {
                Element priceElement = doc.selectFirst(".vtex-product-price-1-x-currencyContainer, .vtex-product-price-1-x-sellingPriceValue, .vtex-product-price-1-x-price-selling, .selling-price, .skuBestPrice");
                if (priceElement != null) {
                    price = parsePriceString(priceElement.text());
                }
            }

            // Método 4: Fallback de busca de texto com R$
            if (price == null) {
                for (Element el : doc.select("span, div, p")) {
                    String text = el.text();
                    if (text.contains("R$") && text.length() < 50) {
                        Float tempPrice = parsePriceString(text);
                        if (tempPrice != null) {
                            price = tempPrice;
                            break;
                        }
                    }
                }
            }

            if (price != null) {
                System.out.println("Sucesso! Preço extraído da New Balance: " + price);
                return new CrawlerResult(price, "NEW_BALANCE", url);
            }
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao link New Balance: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro inesperado no crawler New Balance: " + e.getMessage());
        }

        return new CrawlerResult(null, "NEW_BALANCE", url);
    }

    private Float findPriceInJson(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("price")) {
                try {
                    return obj.get("price").getAsFloat();
                } catch (Exception e) {
                    return parsePriceString(obj.get("price").getAsString());
                }
            }
            if (obj.has("offers")) {
                JsonElement offers = obj.get("offers");
                Float price = findPriceInJson(offers);
                if (price != null) return price;
            }
            // Busca recursiva nas chaves do objeto
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                Float price = findPriceInJson(entry.getValue());
                if (price != null) return price;
            }
        } else if (element.isJsonArray()) {
            for (JsonElement item : element.getAsJsonArray()) {
                Float price = findPriceInJson(item);
                if (price != null) return price;
            }
        }
        return null;
    }

    private Float parsePriceString(String priceStr) {
        if (priceStr == null) return null;
        priceStr = priceStr.replaceAll("[^0-9,. ]", "").trim();
        if (priceStr.isEmpty()) return null;

        try {
            if (priceStr.contains(",") && priceStr.contains(".")) {
                if (priceStr.lastIndexOf(",") > priceStr.lastIndexOf(".")) {
                    priceStr = priceStr.replace(".", "").replace(",", ".");
                } else {
                    priceStr = priceStr.replace(",", "");
                }
            } else if (priceStr.contains(",")) {
                priceStr = priceStr.replace(",", ".");
            }
            return Float.parseFloat(priceStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
