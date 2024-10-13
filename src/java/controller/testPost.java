
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(name = "testPost", urlPatterns = {"/testPost"})
public class testPost extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        Gson gson = new Gson();
        JsonObject requestJson = gson.fromJson(request.getReader(), JsonObject.class);

        String text = requestJson.get("text").getAsString();
        System.out.println(text);
        
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("text", text);
        
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));
        
        
    }

}
