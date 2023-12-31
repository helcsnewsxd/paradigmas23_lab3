import feed.Article;
import feed.Feed;
import httpRequest.HttpRequestException;
import httpRequest.InvalidUrlTypeToFeedException;
import namedEntity.entities.NamedEntity;
import namedEntity.heuristic.Heuristic;
import namedEntity.heuristic.QuickHeuristic;
import namedEntity.heuristic.RandomHeuristic;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.xml.sax.SAXException;
import scala.Tuple2;
import subscriptions.SimpleSubscription;
import subscriptions.Subscriptions;
import webPageParser.EmptyFeedException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.Scanner;

public class Main {
    private static final String subscriptionsFilePath = "config/subscriptions.json";

    private static void printHelp() {
        System.out.println("Please, call this program in correct way: FeedReader [-ne]");
    }

    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("************* FeedReader version 2.0 (Spark) *************");

        if (args.length > 1 || (args.length == 1 && !args[0].equals("-ne"))) {
            printHelp();
            return;
        }

        // Configuración de la sesión de Spark
        SparkSession sparkSession = SparkSession
                .builder()
                .appName("feedReader")
                .master("local[100]")
                .getOrCreate();

        JavaSparkContext spark = new JavaSparkContext(sparkSession.sparkContext());

        // Deshabilitar los LOGS de INFO (porque ya anda bien)
        spark.setLogLevel("ERROR");

        boolean normalPrint = args.length == 0;

        Subscriptions subscriptions = new Subscriptions(sparkSession);
        subscriptions.parse(subscriptionsFilePath);

        // Paralelizo la lista de las subscripciones para hacerlo de forma concurrente
        JavaRDD<SimpleSubscription> subscriptionList = spark.parallelize(subscriptions.getSubscriptionList());

        // Obtengo todos los feeds
        // Se consideran tuplas (feed, error). Una es null y la otra es dato (se usa
        // para diferenciar)
        JavaRDD<Tuple2<Feed, String>> feeds = subscriptionList
                // Separo las subscripciones por sus parámetros
                .flatMap(simpleSubscription -> {
                    List<Tuple2<SimpleSubscription, Integer>> feedConstructorOptionsList = new ArrayList<>();
                    for (int i = 0, szi = simpleSubscription.getUrlParametersSize(); i < szi; i++)
                        feedConstructorOptionsList.add(new Tuple2<>(simpleSubscription, i));
                    return feedConstructorOptionsList.iterator();
                })
                // Obtengo el feed en base a los parámetros considerados (subscripción y
                // urlParameter)
                // Se devuelve en el formato (feed, error) siendo solo una null en cada tupla
                .map(feedOptions -> {
                    try {
                        Feed actualFeed = feedOptions._1().parse(feedOptions._2());
                        return new Tuple2<>(actualFeed, null);
                    } catch (InvalidUrlTypeToFeedException e) {
                        String actualError = "Invalid URL Type to get feed in "
                                + feedOptions._1().getFormattedUrlForParameter(feedOptions._2());
                        return new Tuple2<>(null, actualError);
                    } catch (IOException e) {
                        String actualError = "IO exception in subscription "
                                + feedOptions._1().getFormattedUrlForParameter(feedOptions._2());
                        return new Tuple2<>(null, actualError);
                    } catch (HttpRequestException e) {
                        String actualError = "Error in connection: " + e.getMessage() + " "
                                + feedOptions._1().getFormattedUrlForParameter(feedOptions._2());
                        return new Tuple2<>(null, actualError);
                    } catch (ParserConfigurationException | ParseException e) {
                        String actualError = "Parse error in "
                                + feedOptions._1().getFormattedUrlForParameter(feedOptions._2());
                        return new Tuple2<>(null, actualError);
                    } catch (SAXException e) {
                        String actualError = "SAX Exception in "
                                + feedOptions._1().getFormattedUrlForParameter(feedOptions._2());
                        return new Tuple2<>(null, actualError);
                    } catch (EmptyFeedException e) {
                        String actualError = "Empty Feed in "
                                + feedOptions._1().getFormattedUrlForParameter(feedOptions._2());
                        return new Tuple2<>(null, actualError);
                    }
                });

        // Preparo la lista de feeds obtenidos
        JavaRDD<Feed> feedList = feeds
                .filter(actualFeed -> actualFeed._1() != null)
                .map(Tuple2::_1);

        // Preparo la lista de errores que sucedieron
        JavaRDD<String> errorList = feeds
                .filter(actualFeed -> actualFeed._2() != null)
                .map(Tuple2::_2);

        // HEURÍSTICA
        // Heurística en uso
        Heuristic heuristicUsed = new QuickHeuristic();

        JavaRDD<Article> articleList = feedList
                // Obtengo todos los artículos
                .flatMap(feed -> feed.getArticleList().iterator());

        // CASOS DE EJECUCIÓN
        if (normalPrint) {
            // Obtener el input de búsqueda sobre los feeds por parte del usuario
            System.out.println("=====================  ¿Qué quiere buscar? Escríbalo en una oración y aprete Enter =====================");
            Scanner scanner = new Scanner(System.in);
            String rawSearchTerms = scanner.nextLine();
            Set<String> searchTerms = new java.util.HashSet<>(Collections.emptySet());

            var terms = rawSearchTerms.split(" ");
            Collections.addAll(searchTerms, terms);

            scanner.close();
            System.out.println("===================== Solicitud recibida con éxito. La estamos procesando. =====================");
                        
            // Ordeno los artículos en base a lo buscado por el usuario
            List<Article> sortedArticles = articleList
                    // Obtengo pares (artículo, entidad)
                    .flatMap(article ->{
                        // Computo las entidades del artículo
                        article.computeNamedEntities(heuristicUsed);
                        
                        List<NamedEntity> namedEntityFullList = article.getNamedEntityList();
                        List<Tuple2<Article, NamedEntity>> namedEntityForArticleList = new ArrayList<>();
                        
                        for(NamedEntity ne : namedEntityFullList){
                            namedEntityForArticleList.add(new Tuple2<>(article, ne));
                        }

                        return namedEntityForArticleList.iterator();
                    })
                    // Filtro aquellos pares cuya entidad esté en la búsqueda del usuario
                    .filter(entityForArticle -> searchTerms.contains(entityForArticle._2().getName()))
                    // Cambio esa NamedEntity por su frecuencia
                    .mapToPair(entityForArticle -> new Tuple2<>(entityForArticle._1(), entityForArticle._2().getFrequency()))
                    // Sumo las frecuencias para cada artículo y obtengo su número para ordenar
                    .reduceByKey(Integer::sum)
                    // Swapeo para poder ordenar basándonos en el número de ocurrencias
                    .mapToPair(Tuple2::swap)
                    // Ordeno descendentemente
                    .sortByKey(false)
                    // Me quedo solo con los artículos a printear
                    .map(Tuple2::_2)
                    // Obtengo la lista para printear
                    .collect();
                    
            // Muestra los artículos al usuario
            for(Article article : sortedArticles){
                article.prettyPrint();
            }
        } else {
            // Obtengo las namedEntity
            List<NamedEntity> namedEntities = articleList
                    .flatMap(article -> {
                        article.computeNamedEntities(heuristicUsed);
                        return article.getNamedEntityList().iterator();
                    })
                    // Obtengo la lista de las entidades
                    .collect();
            
            // Muestro las namedEntity en pantalla
            for(NamedEntity namedEntity : namedEntities){
                System.out.println(namedEntity.getName());
                System.out.println(namedEntity.getFrequency());
                System.out.println(namedEntity.getCategory());
                System.out.println(namedEntity.getTheme());
                System.out.println(namedEntity.getClass().toString());
                System.out.println("-----------");
            }
        }

        // Imprimo los errores en caso que haya habido
        if (!errorList.isEmpty()) {
            System.out.println("==================================================");
            System.out.println(
                    "There was a total of " + errorList.count() + " errors in the creation of the Feeds:");
            errorList.foreach(error -> System.out.println("  - " + error));
        }

        spark.close();
        sparkSession.close();
    }
}