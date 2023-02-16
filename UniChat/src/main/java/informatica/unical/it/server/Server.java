package informatica.unical.it.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable
{
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    public void startServer()
    {
        try
        {
            serverSocket=new ServerSocket(8080);
            executorService= Executors.newCachedThreadPool();
            Database.getInstance().init();
            Thread t=new Thread(this);
            t.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                Socket socket= serverSocket.accept();
                ClientToServerMessages c=new ClientToServerMessages(socket);
                executorService.submit(c);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }
}
