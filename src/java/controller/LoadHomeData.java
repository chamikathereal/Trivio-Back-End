package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.User;
import entity.User_Status;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "LoadHomeData", urlPatterns = {"/LoadHomeData"})
public class LoadHomeData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //LoadHomeData?id=1
        Gson gson = new Gson();

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);
        responseJson.addProperty("message", "Unable to process your request");

        try {

            Session session = HibernateUtil.getSessionFactory().openSession();

            // Get user ID from request parameters
            String userId = request.getParameter("id");

            // user object
            User user = (User) session.get(User.class, Integer.parseInt(userId));

            // user status = 1 (online)
            User_Status user_Status = (User_Status) session.get(User_Status.class, 1);

            // Update user status
            user.setUser_Status(user_Status);
            session.update(user);

            // other users
            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Restrictions.ne("id", user.getId()));
            List<User> otherUserList = criteria.list();

            // other user one by one
            JsonArray jsonChatArray = new JsonArray();
            
            // set unseen msg count as 0
            int unseenMessageCount = 0;
            
            for (User otherUser : otherUserList) {

                //last conversation
                Criteria criteria2 = session.createCriteria(Chat.class);

                criteria2.add(Restrictions.or(
                        Restrictions.and(
                                Restrictions.eq("from_user", user),
                                Restrictions.eq("to_user", otherUser)
                        ),
                        Restrictions.and(
                                Restrictions.eq("from_user", otherUser),
                                Restrictions.eq("to_user", user)
                        )
                )
                );

                List<Chat> chatList1 = criteria2.list();

                for (Chat chat : chatList1) {
                    if (chat.getTo_user().equals(user) && chat.getChat_Status().getId() == 2) {
                        unseenMessageCount++;
                    }
                }

                criteria2.addOrder(Order.desc("id"));
                criteria2.setMaxResults(1);
                
                SimpleDateFormat registerdDate = new SimpleDateFormat("YYY:MMM:dd");

                // Create chat item JSON to send to frontend
                JsonObject jsonChatItem = new JsonObject();
                jsonChatItem.addProperty("other_user_id", otherUser.getId());
                jsonChatItem.addProperty("other_user_mobile", otherUser.getMobile());
                jsonChatItem.addProperty("other_user_name", otherUser.getName());
                jsonChatItem.addProperty("other_user_status", otherUser.getUser_Status().getId()); // 1 - online, 2 - offline
                jsonChatItem.addProperty("other_user_registerd_date", registerdDate.format(otherUser.getRegisterd_date_time()));

                // Check avatar image
                String serverPath = request.getServletContext().getRealPath("");
                String otherUserAvatarImagePath = serverPath + File.separator + "userDPImages" + File.separator + otherUser.getMobile() + ".png";
                File otherUserDPImageFile = new File(otherUserAvatarImagePath);

                if (otherUserDPImageFile.exists()) {
                    // Avatar image found
                    jsonChatItem.addProperty("profile_dp_found", true);
                } else {
                    // Avatar image not found
                    jsonChatItem.addProperty("profile_dp_found", false);
                }

                // Get chat list
                List<Chat> dbChatList = criteria2.list();
                
                Date currentDate = new Date();
                
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                SimpleDateFormat fulltimeFormat = new SimpleDateFormat("hh:mm:ss a");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd");
                SimpleDateFormat currentDateFormat = new SimpleDateFormat("yyyy-MMM-dd");

                if (dbChatList.isEmpty()) {
                    // No chat
                    jsonChatItem.addProperty("message", "Say Hi! From Trivio.");
                    jsonChatItem.addProperty("datetime", dateFormat.format(user.getRegisterd_date_time()));
                    jsonChatItem.addProperty("chat_status_id", 1); // 1 - seen, 2 - unseen
                } else {

                    // Found last chat
                    jsonChatItem.addProperty("message", dbChatList.get(0).getMessage());
                    jsonChatItem.addProperty("time", timeFormat.format(dbChatList.get(0).getDate_time()));
                    jsonChatItem.addProperty("fullTime", fulltimeFormat.format(dbChatList.get(0).getDate_time()));
                    jsonChatItem.addProperty("date", dateFormat.format(dbChatList.get(0).getDate_time()));
                    jsonChatItem.addProperty("currentDate", currentDateFormat.format(currentDate));
                    jsonChatItem.addProperty("chat_status_id", dbChatList.get(0).getChat_Status().getId()); // 1 - seen, 2 - unseen
                    jsonChatItem.addProperty("message_sender_id", dbChatList.get(0).getFrom_user().getId());
                    jsonChatItem.addProperty("unseen_count", unseenMessageCount);
                }
                // Get last conversation
                jsonChatArray.add(jsonChatItem);
                unseenMessageCount = 0;
            }

            // Send users
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "Success");
            //responseJson.add("user", gson.toJsonTree(user));
            responseJson.add("jsonChatArray", gson.toJsonTree(jsonChatArray));

            session.beginTransaction().commit();
            session.close();

            // Send the response as JSON
        } catch (Exception e) {
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));

    }
}
