package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import entity.User_Status;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import model.HibernateUtil;
import model.Validations;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@MultipartConfig
@WebServlet(name = "SignUp", urlPatterns = {"/SignUp"})
public class SignUp extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();

        responseJson.addProperty("success", false);

        //JsonObject requestJson = gson.fromJson(request.getReader(), JsonObject.class);
        String mobile = request.getParameter("mobile");
        String userName = request.getParameter("userName");
        String password = request.getParameter("password");
        Part dpImage = request.getPart("dpImage");

        //validation part
        if (mobile.isEmpty()) {
            
            responseJson.addProperty("message", "Please Enter Your Mobile Number.");
            
        } else if (!Validations.isMobileNumberValid(mobile)) {
            
            responseJson.addProperty("message", "Invalid Mobile Number.");
            
        } else if (userName.isEmpty()) {
            
            responseJson.addProperty("message", "Please Enter Your Name.");
            
        } else if (password.isEmpty()) {
            
            responseJson.addProperty("message", "Please Enter Your Password.");
            
        } else if (!Validations.isPasswordValid(password)) {
            
            responseJson.addProperty("message", "Password must be at least 8 characters, with one uppercase letter, one number, and one special character.");
            
        } else {
            
            // sign up process
            Session session = HibernateUtil.getSessionFactory().openSession();

            //search mobile from db
            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Restrictions.eq("mobile", mobile));

            if (!criteria.list().isEmpty()) {
                //Already Used
                responseJson.addProperty("message", "This Mobile Number Already Used In Trivio");
            } else {

                User user = new User();
                user.setName(userName);
                user.setMobile(mobile);
                user.setPassword(password);
                user.setRegisterd_date_time(new Date());

                //user status 2 (offline)
                User_Status user_Status = (User_Status) session.get(User_Status.class, 2);
                user.setUser_Status(user_Status);

                session.save(user);
                session.beginTransaction().commit();

                // DP image uploaded ?
                if (dpImage != null) {
                    
                    //Dp Image Selected
                    String serverPath = request.getServletContext().getRealPath("");
                    String dpImagePath = serverPath + File.separator + "userDPImages" + File.separator + mobile + ".png";
                    File file = new File(dpImagePath);

                    Files.copy(dpImage.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "Trivo Account Created Successfully. Please Log In!");

                session.close();
            }

        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));
    }
}
