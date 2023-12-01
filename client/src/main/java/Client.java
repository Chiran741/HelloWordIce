import java.util.Objects;
import java.util.Scanner;
import Demo.PrinterPrx;
import com.zeroc.Ice.Communicator;

public class Client
{
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        java.util.List<String> extraArgs = new java.util.ArrayList<>();

        try (Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.client", extraArgs))
        {
            PrinterPrx printer = PrinterPrx.checkedCast(communicator.propertyToProxy("Printer.Proxy"));

            if (printer == null)
            {
                throw new Error("Invalid proxy");
            }
            printer.printString("FWhoami");
            String s = "";
            while (!Objects.equals(s, "exit"))
            {
                s = scanner.nextLine();
                if (Objects.equals(s, "exit"))
                {
                    break;
                }

                System.out.println("Sending: " + s);

                long startTime = System.currentTimeMillis(); // Marca de tiempo antes de enviar la solicitud
                String response = printer.printString(s);
                long endTime = System.currentTimeMillis(); // Marca de tiempo después de recibir la respuesta

                System.out.println("Received: " + response);

                long elapsedTime = endTime - startTime;
                System.out.println("Response time: " + elapsedTime + " milliseconds");
            }
        }
    }
}
