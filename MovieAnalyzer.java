import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByKey;

public class MovieAnalyzer {

    private List<Movie> movies;
    public MovieAnalyzer(String dataset_path) throws IOException {
        this.movies =  readMovies(dataset_path);
    }

    public Map<Integer, Integer> getMovieCountByYear(){
        Map<Integer, Integer> ans = new LinkedHashMap();
        this.movies.stream().
                collect(Collectors.
                        groupingBy(Movie::getReleasedYear,
                                Collectors.summingInt(e -> 1))).entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByKey().reversed())
                .forEachOrdered(e -> ans.put(e.getKey(), e.getValue()));
        return ans;
    }

    public Map<String, Integer> getMovieCountByGenre(){
        Map<String, Integer> ans = new LinkedHashMap();

        List<String> list = new LinkedList<>();
        this.movies.forEach(e -> {
            String[] tem = e.getGenre().split(" ");
            for (String t: tem) {
                list.add(t);
            }
        });

        list.stream()
                .filter(e -> !e.equals(""))
                .collect(Collectors
                        .groupingBy(String::valueOf,
                                Collectors.summingInt(e -> 1))).entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByKey())
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEachOrdered(e -> ans.put(e.getKey(), e.getValue()));

        return  ans;
    }


    public Map<List<String>, Integer> getCoStarCount(){
        Map<List<String>, Integer> ans = new LinkedHashMap();
        List<List<String>> list = new LinkedList<>();

        this.movies.forEach(e -> {
            List<String> tem = new LinkedList<>();
            tem.add(e.getStar1());
            tem.add(e.getStar2());
            tem.add(e.getStar3());
            tem.add(e.getStar4());
            tem = tem.stream().sorted().collect(Collectors.toList());

            List<String> tem2 = new LinkedList<>();
            tem2.add(tem.get(0));
            tem2.add(tem.get(1));
            list.add(tem2);

            List<String> tem3 = new LinkedList<>();
            tem3.add(tem.get(0));
            tem3.add(tem.get(2));
            list.add(tem3);

            List<String> tem4 = new LinkedList<>();
            tem4.add(tem.get(0));
            tem4.add(tem.get(3));
            list.add(tem4);

            List<String> tem5 = new LinkedList<>();
            tem5.add(tem.get(1));
            tem5.add(tem.get(2));
            list.add(tem5);

            List<String> tem6 = new LinkedList<>();
            tem6.add(tem.get(1));
            tem6.add(tem.get(3));
            list.add(tem6);

            List<String> tem7 = new LinkedList<>();
            tem7.add(tem.get(2));
            tem7.add(tem.get(3));
            list.add(tem7);
        });

        list.stream()
                .collect(Collectors
                        .groupingBy(List::copyOf,
                                Collectors.summingInt(e -> 1))).entrySet().stream()
                .sorted(Map.Entry.<List<String>, Integer>comparingByValue().reversed())
//                .sorted(Map.Entry.<List<String>, Integer>comparingByKey())
                .forEachOrdered(e -> ans.put(e.getKey(), e.getValue()));

        return ans;
    }

    public List<String> getTopMovies(int top_k, String by){
        List<String> ans = new LinkedList<>();

        if (by.equals("runtime")){
            this.movies.stream().sorted(Comparator.comparing(Movie::getRuntimeInt).reversed()
                            .thenComparing(Movie::getSeriesTitle))
                    .limit(top_k).forEach(a -> ans.add(a.getSeriesTitle()));
        }
        else {
            this.movies.stream().sorted(Comparator.comparing(Movie::getOverviewLen).reversed()
                            .thenComparing(Movie::getSeriesTitle))
                    .limit(top_k).forEach(a -> ans.add(a.getSeriesTitle()));
        }

        return ans;
    }

    public List<String> getTopStars(int top_k, String by){
        List<Star> stars_r = new LinkedList<>();
        List<Star> stars = new LinkedList<>();
        List<String> ans = new LinkedList<>();
        this.movies.stream().forEach(a ->{
            float rating = a.getImdmRating();
            String gross = a.getGross();
            if (!gross.equals("null")){
                Star tem1 = new Star(a.getStar1(),rating, gross);
                stars.add(tem1);
                Star tem2 = new Star(a.getStar2(),rating, gross);
                stars.add(tem2);
                Star tem3 = new Star(a.getStar3(),rating, gross);
                stars.add(tem3);
                Star tem4 = new Star(a.getStar4(),rating, gross);
                stars.add(tem4);
            }
            Star tem1 = new Star(a.getStar1(),rating, "1234");
            stars_r.add(tem1);
            Star tem2 = new Star(a.getStar2(),rating, "1234");
            stars_r.add(tem2);
            Star tem3 = new Star(a.getStar3(),rating, "1234");
            stars_r.add(tem3);
            Star tem4 = new Star(a.getStar4(),rating, "1234");
            stars_r.add(tem4);

        });

        if (by.equals("rating")){
            stars_r.stream()
                    .collect(Collectors
                            .groupingBy(Star::getName, Collectors.averagingDouble(Star::getRating)))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed().thenComparing(comparingByKey()))
                    .limit(top_k)
                    .forEachOrdered(e -> ans.add(e.getKey()));
        }

        else {
            stars.stream()
                    .collect(Collectors
                            .groupingBy(Star::getName, Collectors.averagingLong(Star::getGross)))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed().thenComparing(comparingByKey()))
                    .limit(top_k)
                    .forEachOrdered(e -> ans.add(e.getKey()));
        }

        return ans;
    }

    public List<String> searchMovies(String genre, float min_rating, int max_runtime){
        List<String> ans = new LinkedList<>();
        this.movies.stream().filter(a -> {
                    LinkedList<String> genres = new LinkedList<>(Arrays.asList(a.getGenre().split(" ")));
                    return genres.contains(genre);
                })
                .filter(a -> a.getImdmRating() >= min_rating)
                .filter(a -> a.getRuntimeInt() <= max_runtime)
                .sorted(Comparator.comparing(Movie::getSeriesTitle))
                .forEach(a -> {
                    ans.add(a.getSeriesTitle());
                });
        return ans;
    }


    public static List<Movie> readMovies(String filename) throws IOException
    {
        FileInputStream inputStream = new FileInputStream(filename);
        return  Files.lines(Paths.get(filename))
                .filter(a -> a.startsWith("\""))
                .map(l -> l.split("(?<=\")"))
                .map(l -> {
                    String a = "";
                    for (int i = 0; i < l.length; i++) {
                        if (i % 2 == 0 )a += l[i];
                        else a += l[i].replace(",", " ");
                    }
                    if (a.substring(a.length()-1).equals(","))  a += "null";
                    return a;
                })
                .map(l -> l.split(","))
                .map(l -> {String tem = l[1];
                    for (int i = 0; i < tem.length(); i++) {
                        tem = tem.replace("  ", ", ");
                    }
                    l[1] = tem;
                    return l;
                })
                .map(l -> {
                    if (l[2].equals("")) l[2] = "-1";
                    if (l[6].equals("")) l[6] = "-1";
                    if (l[7].startsWith("\"")) l[7] = l[7].substring(1,l[7].length()-1);
                    if (l[8].equals("")) l[8] = "-1";
                    if (l[14].equals("")) l[14] = "-1";

                    return l;
                })
                .map(a -> new Movie(a[1], Integer.parseInt(a[2]), a[3], a[4], a[5],
                        Float.parseFloat(a[6]), a[7], Double.parseDouble(a[8]), a[9], a[10], a[11], a[12],
                        a[13], Long.parseLong(a[14]), a[15]))
                .collect(Collectors.toList());
    }
}

class Movie{
    private String seriesTitle;
    private int releasedYear;
    private String certificate;
    private String runtime;
    private String genre;
    private float imdmRating;
    private String overview;
    private double metaScore;
    private String director;
    private String star1;
    private String star2;
    private String star3;
    private String star4;
    private long noOfVotes;
    private String gross;

    public Movie(String seriesTitle, int releasedYear, String certificate, String runtime, String genre, float imdmRating, String overview, double metaScore, String director, String star1, String star2, String star3, String star4, long noOfVotes, String gross) {
        this.seriesTitle = seriesTitle;
        this.releasedYear = releasedYear;
        this.certificate = certificate;
        this.runtime = runtime;
        this.genre = genre;
        this.imdmRating = imdmRating;
        this.overview = overview;
        this.metaScore = metaScore;
        this.director = director;
        this.star1 = star1;
        this.star2 = star2;
        this.star3 = star3;
        this.star4 = star4;
        this.noOfVotes = noOfVotes;
        this.gross = gross;
    }

    public String getSeriesTitle() {
        return seriesTitle.replace("\"", "");
    }

    public int getReleasedYear() {
        return releasedYear;
    }

    public String getCertificate() {
        return certificate;
    }

    public String getRuntime() {
        return runtime;
    }

    public int getRuntimeInt(){
        int runtimeInt = Integer.parseInt(this.runtime.split(" ")[0]);
        return runtimeInt;
    }

    public int getOverviewLen(){
        int len = this.overview.length();

        return len;
    }

    public String getGenre() {
        return genre.replace("\"", "");
    }

    public float getImdmRating() {
        return imdmRating;
    }

    public String getOverview() {
        return overview;
    }

    public double getMetaScore() {
        return metaScore;
    }

    public String getDirector() {
        return director;
    }

    public String getStar1() {
        return star1;
    }

    public String getStar2() {
        return star2;
    }

    public String getStar3() {
        return star3;
    }

    public String getStar4() {
        return star4;
    }

    public long getNoOfVotes() {
        return noOfVotes;
    }

    public String getGross() {
        return gross;
    }
}

class Star {
    private String name;
    private float rating;
    private Long gross;

    public Star(String name, float rating, String gross) {
        this.name = name;
        this.rating = rating;
        this.gross = Long.parseLong(gross.replace(",","").replace("\"", "").replace(" ",""));
    }

    public String getName() {
        return name;
    }

    public float getRating() {
        return rating;
    }

    public Long getGross() {
        return gross;
    }
}