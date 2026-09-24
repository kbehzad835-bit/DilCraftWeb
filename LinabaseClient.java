package ir.dilcraft;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.*;

public final class LinabaseClient {
    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10)).build();
    private final String base, key, table, userCol, dilCol;

    public LinabaseClient(String base, String key, String table,
                           String userCol, String dilCol) {
        this.base=base.replaceAll("/+$","");
        this.key=key; this.table=table;
        this.userCol=userCol; this.dilCol=dilCol;
    }

    public long getBalance(String username) throws Exception {
        String u=URLEncoder.encode(username, StandardCharsets.UTF_8).replace("+","%20");
        String url=base+"/rest/v1/"+table+"?"+userCol+"=eq."+u+
                   "&select="+dilCol+"&limit=1";
        HttpRequest r=req(url).GET().build();
        HttpResponse<String> x=http.send(r,HttpResponse.BodyHandlers.ofString());
        if(x.statusCode()/100!=2) throw new Exception("HTTP "+x.statusCode()+": "+x.body());
        Matcher m=Pattern.compile("""+Pattern.quote(dilCol)+""\\s*:\\s*(-?\\d+)").matcher(x.body());
        if(!m.find()) throw new Exception("Player not found.");
        return Long.parseLong(m.group(1));
    }

    public void setBalance(String username,long amount) throws Exception {
        String u=URLEncoder.encode(username,StandardCharsets.UTF_8).replace("+","%20");
        String url=base+"/rest/v1/"+table+"?"+userCol+"=eq."+u;
        String json="{""+dilCol+"":"+amount+"}";
        HttpRequest r=req(url).method("PATCH",HttpRequest.BodyPublishers.ofString(json))
            .header("Content-Type","application/json")
            .header("Prefer","return=minimal").build();
        HttpResponse<String> x=http.send(r,HttpResponse.BodyHandlers.ofString());
        if(x.statusCode()/100!=2) throw new Exception("HTTP "+x.statusCode()+": "+x.body());
    }

    private HttpRequest.Builder req(String url){
        return HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(10))
            .header("Authorization","Bearer "+key)
            .header("apikey",key)
            .header("Accept","application/json");
    }
}
