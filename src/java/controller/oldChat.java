package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.Chat_Status;
import entity.User;
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

@WebServlet(name = "LoadChat", urlPatterns = {"/LoadChat"})
public class oldChat extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // LoadChat?logged_user_id=1&other_user_id=2
        Gson gson = new Gson();

        Session session = HibernateUtil.getSessionFactory().openSession();

        String logged_user_id = request.getParameter("logged_user_id");
        String other_user_id = request.getParameter("other_user_id");
        String initialLoad = request.getParameter("initialLoad");

        User logged_user = (User) session.get(User.class, Integer.parseInt(logged_user_id));
        User other_user = (User) session.get(User.class, Integer.parseInt(other_user_id));

        Criteria criteria = session.createCriteria(Chat.class);
        criteria.add(
                Restrictions.or(
                        Restrictions.and(Restrictions.eq("from_user", logged_user), Restrictions.eq("to_user", other_user)),
                        Restrictions.and(Restrictions.eq("from_user", other_user), Restrictions.eq("to_user", logged_user))
                )
        );

        // Sort chats by date and time
        criteria.addOrder(Order.asc("date_time"));

        // Get the list of chat messages
        List<Chat> chat_list = criteria.list();

        // Get chat status = 1 (seen)
        Chat_Status chat_status = (Chat_Status) session.get(Chat_Status.class, 1);

        // Create chat array to hold JSON response
        JsonArray chatArray = new JsonArray();

        // Date formatters
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd");
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MMM-dd hh:mm a");

        // Track the previous message's date to identify date changes
        String previousDate = "";

        for (Chat chat : chat_list) {

            // Format the current message date
            String currentDate = dateFormat.format(chat.getDate_time());

            // Check if the date of the current message is different from the previous one
            if (!currentDate.equals(previousDate)) {
                // Add the date as a special entry in the chat array
                JsonObject dateObject = new JsonObject();
                dateObject.addProperty("date", currentDate);
                dateObject.addProperty("type", "date");  // Mark this entry as a date separator
                chatArray.add(dateObject);

                // Update the previousDate tracker
                previousDate = currentDate;
            }

            // Create chat object
            JsonObject chatObject = new JsonObject();

            // Add chat message properties
            chatObject.addProperty("message", chat.getMessage());
            chatObject.addProperty("time", timeFormat.format(chat.getDate_time()));
            chatObject.addProperty("date", currentDate);  // This is optional since the date is already added as a separator
            chatObject.addProperty("dateAndTime", dateTimeFormat.format(chat.getDate_time()));

            //get chats only from other user
            if (chat.getFrom_user().getId() == other_user.getId()) {

                //add side to chat object
                chatObject.addProperty("side", "left");

                //get only unseen chats (chat_status_id = 2)
                if ("true".equals(initialLoad) && chat.getChat_Status().getId() == 2) {
                    //update chat status to seen
                    chat.setChat_Status(chat_status);
                    session.update(chat);
                }
            } else {
                //get chat from logged user
                //add side to chat object
                chatObject.addProperty("side", "right");
                chatObject.addProperty("status", chat.getChat_Status().getId()); //1seen, 2= unseen
            }

            // Add the chat message to the array
            chatArray.add(chatObject);
        }

        // Commit transaction and close session
        session.beginTransaction().commit();
        session.close();

        // Send response back to the client
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(chatArray));
    }
}
