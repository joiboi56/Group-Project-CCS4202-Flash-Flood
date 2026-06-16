import controller.ApiServer;
import controller.FloodController;
import database.FloodDatabase;

public class Main {
    public static void main(String[] args) throws Exception {
        FloodDatabase database = new FloodDatabase();
        FloodController controller = new FloodController(database);

        int port = 8080;
        String webRoot = "webapp";

        ApiServer server = new ApiServer(controller, port, webRoot);
        server.start();
    }
}
