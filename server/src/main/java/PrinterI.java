
import Demo.Printer;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.UserException;
import com.zeroc.IceInternal.Incoming;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.CompletionStage;

public class PrinterI implements Demo.Printer {
    private final Object ansLock = new Object();
    private String ans = "";
    private ArrayList<String> users = new ArrayList<>();

    public String printString(String s, Current current) {
        try {
            return m(s);
        } catch (com.zeroc.Ice.TimeoutException e) {
            System.out.println("TimeoutException: " + e.getMessage());
            return "TimeoutException: " + e.getMessage();
        }
    }

    public String m(String s) {
        try {
            String name = getLoggedInUsername().trim() + ": ";
            if (canParseToInt(s)) {
                Thread primeFactorsThread = new Thread(() -> {
                    String primeFactors = primeFactorsToString(s);
                    synchronized (ansLock) {
                        ans = name + primeFactors;
                    }
                });
                primeFactorsThread.start();
                primeFactorsThread.join();
            } else if (s.startsWith("!")) {
                String command = s.substring(1); // Obtener el comando sin el "!"
                Thread executeCommandThread = new Thread(() -> {
                    String result = null;
                    try {
                        result = executeCommand(command);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    synchronized (ansLock) {
                        ans = name + result;
                    }
                });
                executeCommandThread.start();
                executeCommandThread.join();
            } else if (s.startsWith("listports")) {
                String[] spl = s.split(":");
                Thread scanPortsThread = new Thread(() -> {
                    String portScanResult = scanPorts(spl[1]);
                    synchronized (ansLock) {
                        ans = name + portScanResult;
                    }
                });
                scanPortsThread.start();
                scanPortsThread.join();
            } else if (s.startsWith("listifs")) {
                Thread ifconfigThread = new Thread(() -> {
                    String ifconfigResult = null;
                    try {
                        ifconfigResult = executeCommand("ifconfig");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    synchronized (ansLock) {
                        ans = name + ifconfigResult;
                    }
                });
                ifconfigThread.start();
                ifconfigThread.join();
            } else if (s.equals("FWhoami")){
                String user =  getLoggedInUsername();
                boolean found = false;
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).equals(user)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    users.add(user);
                }
            } else if(s.equals("list clients")){
                ans = "\n";
                for (int i = 0; i < users.size(); i++) {
                    ans += users.get(i) + "\n";
                }
            }else {
                synchronized (ansLock) {
                    ans = name + s;
                }
            }
        } catch (IOException | InterruptedException e) {
            synchronized (ansLock) {
                ans = "Error: " + e.getMessage();
            }
        }
        synchronized (ansLock) {
            return ans;
        }
    }

    private static String getLoggedInUsername() throws IOException {
        return executeCommand("whoami");
    }

    public static boolean canParseToInt(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String scanPorts(String ipAddress) {
        StringBuilder scanResult = new StringBuilder();

        try {
            Process process = Runtime.getRuntime().exec("nmap " + ipAddress);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                scanResult.append(line).append("\n");
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return scanResult.toString();
    }

    public static String executeCommand(String command) throws IOException {
        String text = "";
        try {
            Process proc = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                text += line + "\n";
            }
            proc.waitFor();
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
        return text;
    }

    public static String primeFactorsToString(String num) {
        int n = Integer.parseInt(num);
        StringBuilder result = new StringBuilder();

        // Divide n por 2 hasta que sea impar
        while (n % 2 == 0) {
            result.append(2).append("*");
            n /= 2;
        }

        // Ahora n es impar, buscamos factores primos mayores
        for (int i = 3; i <= Math.sqrt(n); i += 2) {
            while (n % i == 0) {
                result.append(i).append("*");
                n /= i;
            }
        }

        // Si n es mayor a 2, es un primo restante
        if (n > 2) {
            result.append(n);
        } else {
            // Eliminar el último "*"
            result.deleteCharAt(result.length() - 1);
        }

        return result.toString();
    }

    @Override
    public String[] ice_ids(Current current) {
        return Printer.super.ice_ids(current);
    }

    @Override
    public String ice_id(Current current) {
        return Printer.super.ice_id(current);
    }

    @Override
    public CompletionStage<com.zeroc.Ice.OutputStream> _iceDispatch(Incoming in, Current current) throws UserException {
        return Printer.super._iceDispatch(in, current);
    }
}
