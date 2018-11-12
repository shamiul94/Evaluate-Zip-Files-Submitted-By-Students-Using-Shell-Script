package sample;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Controller {
    public Button bstart;
    public TextArea textArea1;
    public Button bstop;
    public TextArea textArea2;


    private ServerSocket introSocket;
    private Socket connectionSocket;
    private boolean stopped;
    private Service<Void> service;
    static Vector<ClientDealer> ar = new Vector<>();
    static Hashtable<String,fileStore> hTable=new Hashtable<>();


    int online=0;
    static long maxBuffer=1000000;
    int current=0;
    ClientDealer active = null;
    static int i = 0;
    int p=0;
    int fileRecognition=0;
    @FXML
    public void initialize() throws IOException {
        textArea1.setEditable(false);
        textArea1.setText("<-----------Welcome---------->\n");
        textArea2.setText("<-----------Online---------->\n");
        stopped=true;
        bstop.setDisable(true);
        introSocket = new ServerSocket(6789);
        connectionSocket=null;
    }



    public void startClicked(ActionEvent actionEvent) throws IOException {
        stopped=false;
        bstop.setDisable(false);
        bstart.setDisable(true);

        service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {

                return new Task<Void>() {

                    @Override
                    protected synchronized Void call() throws Exception {

                        while (true){
                            if (stopped) break;
                            try {
                                connectionSocket = introSocket.accept();
                            } catch (IOException e) {
                                System.out.println("void call e");
                                e.printStackTrace();
                                break;
                            }
                            DataInputStream dis = new DataInputStream(connectionSocket.getInputStream());
                            DataOutputStream dos = new DataOutputStream(connectionSocket.getOutputStream());

                            int j=-1;
                            while(j<1) {
                                String client = dis.readUTF();


                                for (ClientDealer mc : Controller.ar) {
                                    //System.out.println(mc.name);
                                    //mc.name.equals(recipient) &&
                                    if (mc.name.equals(client) && mc.isLoggedin == true) {

                                        active = mc;
                                        //dos.writeUTF("Not a valid Id");
                                        //System.out.println("paisi");
                                        break;
                                    }

                                }

                                if (active == null) {
                                    dos.writeUTF("valid id");
                                    ClientDealer cl = new ClientDealer(connectionSocket, client, i, dis, dos);
                                    System.out.println(i + " class created");
                                    Task<Void> task = cl;
                                    Thread t = new Thread(task);
                                    t.setDaemon(true);
                                    ar.add(cl);
                                    t.start();

                                    textArea1.appendText("Client [" + i + "] is now connected.\n");
                                    textArea2.appendText(cl.id + "\n");
                                    i++;
                                    break;
                                } else {
                                    dos.writeUTF("T");
                                    active=null;
                                }
                            }



                        }
                        return null;
                    }
                };
            }



            @Override
            protected void cancelled() {
                super.cancelled();
                textArea1.appendText("Service Canceled\n");
            }
        };

        service.start();


    }



    public class ClientDealer extends Task<Void>
    {
        int id;
        private String name;
        final DataOutputStream dos;
        final DataInputStream dis;
        Socket connectionSocket;
        boolean isLoggedin;
        String receivedactual=null;
        String MsgToSend=null;
        String recipient=null;
        String s;
        String Id;


        public ClientDealer(Socket s,String name,Integer id, DataInputStream dis, DataOutputStream dos) {
            this.name = name;
            this.connectionSocket=s;
            this.id=id;
            this.dos=dos;
            this.dis=dis;
            this.isLoggedin=true;
        }


        public int power(int a, int b)
        {
            int power = 1;
            for(int c=0;c<b;c++)
                power*=a;
            return power;
        }
        public  int stringToInt(String s)
        {
            int res=0;

            for (int i = s.length()-1; i>=0 ; i--) {
                if(s.charAt(i)=='1') res+=power(2,s.length()-i-1);

            }
            return res;
        }

        public  int chesksum(String st)
        {
            int i = 0;
            int count=0;
            while(i!=st.length())
            {
                if(st.charAt(i)=='1') count+=1;

                i++;
            }


            return count%256;
        }
        public String intToString(int number) {
            StringBuilder result = new StringBuilder();

            for(int i = 7; i >= 0 ; i--) {
                int mask = 1 << i;
                result.append((number & mask) != 0 ? "1" : "0");

                //if (i % groupSize == 0)
                //result.append(" ");
            }
            //result.replace(result.length() - 1, result.length(), "");

            return result.toString();
        }
        public String byteToString(byte[] cmp)
        {
            String asas="";
            for (int i = 0; i <cmp.length ; i++) {
                //System.out.println((char)cmp[i]);
                System.out.println("bytetostring");
                System.out.println((int) cmp[i]);
                //System.out.println(Integer.toBinaryString( (int) component[i]));
                //s+=Integer.toBinaryString( (int) component[i]);
                //String o;
                //System.out.println(o=intToString( (int) cmp[i]));
                asas+=intToString((int) cmp[i]);

            }
            return asas;

        }










        @Override
        protected Void call() throws Exception {

            String received;

            while(true)
            {
                try{

                    //System.out.println(MsgToSend+":"+id);
                    received=dis.readUTF();
                    //System.out.println(received+":"+id);
                    if(received.equals("ok") && fileRecognition==0)
                    {
                        //System.out.println(MsgToSend+"ok er vitre");
                        //this.dos.writeUTF(MsgToSend);
                        //System.out.println("joy bangla");
                        for (ClientDealer mc : Controller.ar)
                        {
                            //System.out.println(mc.name);
                            //mc.name.equals(recipient) &&
                            if (mc.name.equals(s) && mc.isLoggedin==true)
                            {

                                this.dos.writeUTF(mc.MsgToSend);
                                //System.out.println("paisi");
                                break;
                            }

                        }

                    }
                    else if(received.equals("no"))
                    {
                        for (ClientDealer mc : Controller.ar)
                        {
                            //System.out.println(mc.name);
                            //mc.name.equals(recipient) &&
                            if (mc.name.equals(s) && mc.isLoggedin==true)
                            {

                                mc.dos.writeUTF("He is Busy");
                                //System.out.println("paisi");
                                break;
                            }

                        }

                    }
                    else if (received.equals("ok") && fileRecognition==1)
                    {
                        String str;
                        Set<String> keys = hTable.keySet();
                        Iterator<String> itr = keys.iterator();
                        while (itr.hasNext())
                        {
                            str=itr.next();
                            System.out.println(str);
                        }
                        for (ClientDealer mc : Controller.ar)
                        {
                            System.out.println(mc.name);
                            //mc.name.equals(recipient) &&
                            if (mc.name.equals(s) && mc.isLoggedin==true)
                            {
                                String fid=Id;
                                System.out.println(fid);
                                String str1;
                                Set<String> keys1 = hTable.keySet();
                                Iterator<String> itr1 = keys.iterator();
                                while (itr1.hasNext())
                                {
                                    str1=itr1.next();
                                    System.out.println(str1);
                                    if(str1==fid) {
                                        //File fnew=new File("F:\\J\\test\\src\\abc.txt");
                                        //FileInputStream fis= new FileInputStream(fnew);
                                        //BufferedInputStream bis = new BufferedInputStream(fis);

                                        this.dos.writeUTF("Received file size:"+String.valueOf(hTable.get(str1).receive));

                                        if(hTable.get(str1).receive>=hTable.get(str1).fileSize||hTable.get(str1).receive<=hTable.get(str1).fileSize) {
                                            this.dos.writeUTF("f");
                                            dos.flush();
                                            //hTable.get(str1).flush();
                                            System.out.println(hTable.get(str1).receive);
                                            this.dos.writeUTF(String.valueOf(hTable.get(str1).receive));
                                            byte[] com;
                                            int cS=1000;
                                            int curr=0; //ei client k patha nor somoi curr
                                            current+=1000; //total buffer size er current
                                            while (curr<hTable.get(str1).receive) {

                                                if(hTable.get(str1).receive-curr<cS)
                                                {
                                                    cS=hTable.get(str1).receive-curr;
                                                }
                                                System.out.println("to client");
                                                this.dos.write(hTable.get(str1).store,curr,cS);
                                                this.dos.flush();
                                                curr+=cS;
                                            }
                                            current-=1000;



                                            this.dos.writeUTF("Successfully received");
                                            //current -= hTable.get(str1).fileSize;
                                            //System.out.println("current m");
                                            hTable.remove(str1);
                                        }
                                        else {
                                            this.dos.writeUTF("Problem in receiving");
                                            //current-= hTable.get(str1).fileSize;
                                            hTable.remove(str1);
                                        }
                                        break;
                                    }

                                }
                                //System.out.println("paisi");
                                break;
                            }

                        }
                        System.out.println("After deleting");
                        System.out.println(current);
                        String str2;
                        Set<String> keys2 = hTable.keySet();
                        Iterator<String> itr2 = keys2.iterator();
                        while (itr2.hasNext())
                        {
                            str2=itr2.next();
                            System.out.println("This "+str2);
                        }

                        fileRecognition=0;


                    }
                    else if(received.charAt(0)=='&')
                    {
                        int l=received.length();
                        String actual=received.substring(1,l);
                        StringTokenizer tkn = new StringTokenizer(actual, "#");
                        String fName=tkn.nextToken();
                        System.out.println(fName);
                        int fSize= Integer.parseInt(tkn.nextToken());
                        System.out.println(fSize);
                        String cName=tkn.nextToken();
                        System.out.println(cName);



                        //current+=fSize;
                        ClientDealer active = null;
                        for (ClientDealer mc : Controller.ar)
                        {
                            //System.out.println(mc.name);

                            if (mc.name.equals(cName) && mc.isLoggedin==true)
                            {
                                active=mc;
                                active.s=this.name;
                                //active.Id=fId;
                                p=1;
                                //mc.dos.writeUTF("@Do you want to receive from: "+this.name);
                                //mc.s=this.name;
                                //System.out.println(s);
                                //System.out.println("paisi");
                                break;
                            }
                        }
                        if(p==1) {

                            if(current<maxBuffer)
                            {
                                fileStore obj=new fileStore(fName,fSize,cName,this.name);
                                String fId=obj.hCode();
                                System.out.println(fId);
                                active.Id=fId;
                                Random r = new Random();

                                Integer Low = 100;
                                Integer High = 200;
                                Integer Result = 3;
                                        //r.nextInt(High-Low) + Low;
                                current+=Result;
                                String chunk="&"+Result;
                                int cSize=Result;

                                this.dos.writeUTF(chunk);
                                int f=0;
                                if(chunk!=null)
                                {
                                    int currentRight=0;
                                    byte[] b = new byte[100];
                                    File fnew=new File("F:\\J\\test\\src\\abc2.txt");
                                    FileOutputStream fos=new FileOutputStream(fnew);
                                    BufferedOutputStream bos = new BufferedOutputStream(fos);

                                    while (currentRight<fSize)
                                    {
                                        System.out.println("waiting for component");
                                       try {


                                           int s = this.dis.read(b);
                                           //obj.store=concatenateByteArrays(obj.store,b);
                                           System.out.println("server got byte array" + s);
                                           byte[] rread = new byte[s];
                                           System.arraycopy(b, 0, rread, 0, s);
                                           //rread=b;
                                           System.out.println(rread.length);
                                           String frame = byteToString(rread);

                                           System.out.println("Frame len: " + frame.length() + "   " + frame);
                                           int cut = frame.indexOf("01111110", 8);
                                           String playLoad = frame.substring(8, cut);
                                           String destuffed = playLoad.replaceAll("111110", "11111");
                                           System.out.println("Destuffed len: " + destuffed.length() + "   " + destuffed);
                                           String ac_load = destuffed.substring(8, destuffed.length() - 8);
                                           String checkBit = destuffed.substring(destuffed.length() - 8 + 1, destuffed.length());
                                           int got = stringToInt(checkBit);
                                           System.out.println("CheckSum of sender checkbit==> " + got);
                                           int chkload = chesksum(ac_load);
                                           System.out.println("CheckSum of sender load==> " + chkload);

                                           if(chkload == got) {
                                               this.dos.writeUTF("0");
                                               byte[] cmp = new byte[ac_load.length() / 8];
                                               int m = 0;
                                               for (int i = 0; i < ac_load.length() / 8; i++) {

                                                   int k = stringToInt(ac_load.substring(m, m + 8));
                                                   cmp[i] = (byte) k;
                                                   m += 8;
                                               }


                                               obj.makebyte(cmp);
                                               bos.write(cmp, 0, cmp.length);

                                               //System.out.println(s);
                                               currentRight += ac_load.length() / 8;
                                               f += ac_load.length() / 8;
                                               //System.out.println(f);

                                           }
                                           else this.dos.writeUTF("1");
                                       }
                                       catch (Exception e)
                                       {
                                           e.printStackTrace();
                                       }
                                        /*if (f == fSize) {
                                               this.dos.writeUTF("Receiving Completed");
                                       } else {
                                                this.dos.writeUTF("got it");
                                            }*/
                                    }
                                    current-=Result;

                                    obj.receive=f;

                                    bos.flush();
                                    bos.close();
                                    fos.close();
                                }
                                hTable.put(fId,obj);

                                active.dos.writeUTF("@Do you want to receive from: "+this.name+"\nReply 'ok' or 'no'");
                                fileRecognition=1;
                                p=0;
                            }
                            else
                            {
                                this.dos.writeUTF("Server is busy");
                            }
                        }
                        else this.dos.writeUTF("He is not availabe");
                    }

                    else
                    {
                        //System.out.println("not ok er vitre");
                        receivedactual=received;
                        textArea1.appendText("Cliend No. "+id+" says "+receivedactual+"\n");
                        StringTokenizer st = new StringTokenizer(receivedactual, "#");
                        MsgToSend = st.nextToken();
                        //System.out.println(MsgToSend+"else er");
                        recipient = st.nextToken();
                        //System.out.println(recipient+"else er recepient");
                        ClientDealer active=null;
                        for (ClientDealer mc : Controller.ar)
                        {
                            System.out.println(mc.name);

                            if (mc.name.equals(recipient) && mc.isLoggedin==true)
                            {
                                active=mc;
                                active.s=this.name;
                                p=1;

                                break;
                            }
                        }
                        if(p==1) {
                            active.dos.writeUTF("@Do you want to receive from: "+this.name+"\nReply 'ok' or 'no'");
                        p=0;}
                        else this.dos.writeUTF("He is not availabe");

                    }


                    if(received.equals("logout")){
                        //System.out.println("gelam");
                        //this.isLoggedin=false;
                        //this.connectionSocket.close();
                        break;
                    }
                    //System.out.println("1bar ghurse, ok er age");


                }catch (Exception e) {
                    System.out.println("Error! Connection lost for id "+id);
                    //e.printStackTrace();
                    break;
                }

            }
            System.out.println("Client "+this.name+" connection Canceled");
            try
            {
                //System.out.println("try er upore");
                this.connectionSocket.close();
                this.dis.close();
                this.dos.close();
                //System.out.println("try er niche");

            }catch(IOException e){
                System.out.println("try excp");
                e.printStackTrace();
            }

            return null;
        }

    }



    public void bStopClicked(ActionEvent actionEvent) {
        stopped=true;
        bstop.setDisable(true);
        bstart.setDisable(false);
        service.cancel();

    }



}

