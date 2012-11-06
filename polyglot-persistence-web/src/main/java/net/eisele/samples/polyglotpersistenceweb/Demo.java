package net.eisele.samples.polyglotpersistenceweb;

import java.io.IOException;
import java.io.PrintWriter;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.eisele.samples.polyglotpersistencenosqlpu.model.Customer;
import net.eisele.samples.polyglotpersistencenosqlpu.model.Order;
import net.eisele.samples.polyglotpersistencenosqlpu.model.OrderLine;
import net.eisele.samples.polyglotpersistencerationalpu.model.Product;

/**
 *
 * @author eiselem
 */
@WebServlet(name = "Demo", urlPatterns = {"/Demo"})
public class Demo extends HttpServlet {

    @PersistenceUnit(unitName = "composite-pu")
    private EntityManagerFactory emf;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        // Products go into RDBMS
        Product installation = new Product("installation");
        em.persist(installation);

        Product shipping = new Product("shipping");
        em.persist(shipping);

        Product maschine = new Product("maschine");
        em.persist(maschine);

        // Customer into NoSQL
        Customer customer = new Customer();
        customer.setName("myfear");
        em.persist(customer);
        // Order into NoSQL
        Order order = new Order();
        order.setCustomer(customer);
        order.setDescription("Pinball maschine");

        // Order Lines mapping NoSQL --- RDBMS
        order.addOrderLine(new OrderLine(maschine, 2999));
        order.addOrderLine(new OrderLine(shipping, 59));
        order.addOrderLine(new OrderLine(installation, 129));

        em.persist(order);
        em.getTransaction().commit();
        String orderId = order.getId();
        em.close();
        //Force EclipseLink to withdraw caches.
        emf.getCache().evictAll();;

        em = emf.createEntityManager();
        Order order2 = em.find(Order.class, orderId);


        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Polyglot Persistence Demo</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Demo at " + request.getContextPath() + "</h1>");
            out.println("<b>OrderId inserted: " + orderId + "</b>");
            out.println("<br/>");
            out.println("<b>Order found: " + order2.getDescription() + "</b>");
            out.println("<br/>");
            out.println("<br/>");
            for (OrderLine orderLine : order2.getOrderLines()) {
                out.println("With Products: " + orderLine.getProduct().getDescription() + "<br/>");
            }
            out.println("<br/>");
            out.println("</body>");
            out.println("</html>");

            em.close();

        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
