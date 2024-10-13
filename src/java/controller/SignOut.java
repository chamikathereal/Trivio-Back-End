package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import entity.User_Status;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Session;

/**
 *
 * @author ASUS
 */
@WebServlet(name = "SignOut", urlPatterns = {"/SignOut"})
public class SignOut extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);

        try {
            Session session = HibernateUtil.getSessionFactory().openSession();

            // Get user ID from request parameters
            String userId = request.getParameter("id");
            System.out.println(userId);

            // user object
            User user = (User) session.get(User.class, Integer.parseInt(userId));

            if (user != null) {
                // user status = 1 (online)
                User_Status user_Status = (User_Status) session.get(User_Status.class, 2);

                // Update user status
                user.setUser_Status(user_Status);
                session.update(user);

                // Send response
                responseJson.addProperty("success", true);
            }

            session.beginTransaction().commit();
            session.close();
        } catch (Exception e) {
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));

    }

}
