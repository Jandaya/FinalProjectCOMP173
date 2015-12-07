/*
    Joseph Andaya
    COMP173
    Final Project
 */
package finalprojectcomp173;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;


public class ZooSim extends javax.swing.JFrame {

    private File selectedFile;
    private String sFile;
    private JFileChooser fc = new JFileChooser();
    
    private int numVisitors = 4;
    private int visitorsActive = numVisitors;
    private int numCars = 1;
    private int numGasPumps = 3;
    private int waitTime = 1;
    private int visitorsPresent = 0; 
    private Semaphore carSem = new Semaphore(0);
    private Semaphore visitorSem = new Semaphore(0);
    private Semaphore gasSem = new Semaphore(0);
    private Semaphore needGasSem = new Semaphore(0);
    private Semaphore instanceSem = new Semaphore(0);
    private Semaphore notifyMaster = new Semaphore(1);
    private Semaphore gasTruckSem = new Semaphore(0);

    private int integerCount = 4;
    private int lineCount;
    private int allGas = 200;
    
    private boolean isRunning = true;
    
    public Car car = new Car();
    public Visitor visitor = new Visitor();
    
    private int sleepSeconds = 1;

    private int insCount = 0;
    private List<InstanceDay> InstanceList = new ArrayList<InstanceDay>();
    private List<List<Integer>> ListofLists = new ArrayList<List<Integer>>();
    private Iterator allIt = InstanceList.iterator();
    
    
    public class Visitor extends Thread {
        int visitorNum;
        
        public Visitor(){
            visitorNum = 0;
        }
        
        public Visitor(int n){
            visitorNum = n;
        }
       
        
        public void leave(){
            textArea.append("\nVisitor: " + visitorNum + " is done.");
            // when customer leaves decrement
            visitorsPresent--;

            // update number of customers present on GUI
            VisitorsWaitingLabel.setText("Visitors Waiting: " + visitorsPresent);

        }
        
        public void tryRide(){
            try{
                //Try to get a car.
                carSem.acquire(1);
                // takes ride
                suspendSleep(1);
                //once ride finished, they leave the zoo
                leave();
            }
            catch (InterruptedException e){
                System.err.printf("Error on lock");
            }
        }
        public void EnterZoo(){
            // increment the number of visitors in a semaphore
            visitorSem.release(1);
            visitorsPresent++;
            VisitorsWaitingLabel.setText("Visitors Waiting: " + visitorsPresent);
            tryRide();
        }
        public void run(){
            EnterZoo();
        }
    }
    
    public class Car extends Thread{
        private int CarNum;
        private int numRides;
        
        public Car(){
            CarNum = 0;
        }
        
        public Car(int n){
            CarNum = n;
            numRides = 1;
        }
        
        public void drive(){
            numRides--;
            suspendSleep(randomNum(waitTime,1));
        }
        public void getGas(){
            needGasSem.release(1);
            try{
                gasSem.acquire(1);
                textArea.append("Car " + CarNum + " got gas.");
                numRides = 1;
            }
            catch (InterruptedException e){
                        System.err.printf("Error on lock");
            }
        }
        public void startCar(){
            textArea.append("\nCar " + CarNum + "Started");
            while(visitorSem.availablePermits() > 0){
                CarsIdleLabel.setText("Cars at Idle: waiting...");
                // if number of rides ==0 get gas
                if (numRides <= 0){
                    getGas();
                }
                if(visitorSem.availablePermits() <= 0){
                        break;
                    }
                try{
                    // get a customer
                    visitorSem.acquire(1);
                    System.out.println("Car: " + CarNum + " got customer");

                    // perform work on customer
                    //barberStatus.setText("Barber Status: cutting..");
                    //finishCustomer();
                    CarsIdleLabel.setText("Cars at Idle: Running...");
                    carSem.release(1);
                    drive();
                                        
                    // once finished visitor leaves
                    //visitorsPresent--;
                    VisitorsWaitingLabel.setText("Visitors Waiting: " + visitorsPresent);
                }
                catch (InterruptedException e){
                        System.err.printf("Error on lock");
                }
            }
            //isRunning = false;
            CarsIdleLabel.setText("Cars at Idle: stopped...");
            VisitorsWaitingLabel.setText("Visitors Waiting: " + 0);
            instanceSem.release();
            textArea.append("Instance Done: " + CarNum);
            if((instanceSem.availablePermits() >= InstanceList.get(insCount-1).getCars()-1)){
                //insCount++;
                notifyMaster.release();
                System.out.println("Doooooone.");
            }
        }
        
        
        
        
        public void run(){
            startCar();
            // add a car based on loop
            carSem.release(1);
            textArea.append("\nCar " + CarNum+ " Started.");
        }
    }
    
    public class Master extends Thread{
        public Master(){
            
        }
        public void run(){
            try{
                notifyMaster.acquire();
                notifyMaster.drainPermits();
                System.out.println("Master run" );
                
                openZoo(InstanceList.get(insCount));
                insCount++;
                
            }
            catch (InterruptedException e){
                        System.err.printf("Error on lock");
            }
        }
    }
    
    public class Gas extends Thread{
        private int GasLeft;
        private int pumpNumber;
        
        public Gas(){
            //GasLeft = 200;
        }
        public Gas(int n){
            pumpNumber = n;
            //GasLeft = 200;
        }
        public void refillStation(){
            gasTruckLabel.setText("Gas truck status: on the way.");
            suspendSleep(15);
            gasTruckLabel.setText("Gas truck status: Fuel delivered.");
            allGas = 200;
        }
        
        public void openGasStation(){
            textArea.append("\nGas Pump " + pumpNumber+ " on");
            
            
            while(numCars > 0){
                //System.out.println("waiting....");
                try{
                    needGasSem.acquire(1);
                    // cars take 3 seconds to refill
                    suspendSleep(3);
                    gasSem.release(1);
                    allGas--;
                    gasRemainingLabel.setText("Gas Remaining: " + allGas);
                }
                catch (InterruptedException e){
                    System.err.printf("Error on lock");
                }
                
                    if (allGas < 190) {
                        gasTruckSem.release();
                        //refillStation();
                        //textArea.append("Refilling Station");
                        //gasRemainingLabel.setText("Gas Remaining: " + allGas);
                }
                
            }
            
        }
        public void run(){
            gasRemainingLabel.setText("Gas Remaining: " + allGas);
            openGasStation();
            gasSem.release(1);
        }
    }
    
    public class GasTruck extends Thread{
        public GasTruck(){
            
        }
        public void deployGasTruck(){
            try {
                gasTruckSem.acquire();
                gasTruckLabel.setText("Gas truck status: on the way.");
                suspendSleep(15);
                gasTruckLabel.setText("Gas truck status: Fuel delivered.");
                gasTruckSem.drainPermits();
                allGas = 200;
                gasRemainingLabel.setText("Gas Remaining: " + allGas);
                
            } catch (InterruptedException e) {
                System.err.printf("Error on lock");
            }
        }
        public void run(){
            deployGasTruck();
        }
    }
    
    public ZooSim() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField1 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        openFileButton = new javax.swing.JButton();
        openFileLabel = new javax.swing.JLabel();
        runButton = new javax.swing.JButton();
        printButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        VisitorsWaitingLabel = new javax.swing.JLabel();
        CarsIdleLabel = new javax.swing.JLabel();
        gasRemainingLabel = new javax.swing.JLabel();
        instanceLabel = new javax.swing.JLabel();
        gasTruckLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        numVisitorsLabel = new javax.swing.JLabel();
        numCarsLabel = new javax.swing.JLabel();
        carRunTimeLabel = new javax.swing.JLabel();
        numPumpsLabel = new javax.swing.JLabel();

        jTextField1.setText("jTextField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        textArea.setColumns(20);
        textArea.setRows(5);
        jScrollPane1.setViewportView(textArea);

        openFileButton.setText("OpenFile");
        openFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileButtonActionPerformed(evt);
            }
        });

        openFileLabel.setText("File Opened: ");

        runButton.setText("Run");
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(openFileLabel)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(openFileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(runButton)
                        .addGap(34, 34, 34))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(openFileLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openFileButton)
                    .addComponent(runButton))
                .addGap(34, 34, 34))
        );

        printButton.setText("Print");
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtonActionPerformed(evt);
            }
        });

        VisitorsWaitingLabel.setText("Visitors Waiting:");

        CarsIdleLabel.setText("Cars at Idle:");

        gasRemainingLabel.setText("Gas Remaining:");

        instanceLabel.setText("Line Running:");

        gasTruckLabel.setText("Gas truck status: Idle");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(VisitorsWaitingLabel)
                            .addComponent(CarsIdleLabel)
                            .addComponent(gasRemainingLabel)
                            .addComponent(instanceLabel))
                        .addContainerGap(178, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(gasTruckLabel)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(instanceLabel)
                .addGap(8, 8, 8)
                .addComponent(VisitorsWaitingLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(CarsIdleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(gasRemainingLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gasTruckLabel)
                .addContainerGap(51, Short.MAX_VALUE))
        );

        numVisitorsLabel.setText("Initial Visitors:");

        numCarsLabel.setText("Number of Cars:");

        carRunTimeLabel.setText("Car Run Time:");

        numPumpsLabel.setText("Number of Pumps");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(numVisitorsLabel)
                    .addComponent(numCarsLabel)
                    .addComponent(carRunTimeLabel)
                    .addComponent(numPumpsLabel))
                .addContainerGap(135, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(numVisitorsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(numCarsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(carRunTimeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(numPumpsLabel)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(printButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addComponent(printButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void openFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileButtonActionPerformed
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            // clear the list for new file
            selectedFile  = fc.getSelectedFile();
            sFile = selectedFile.toString();
            try {
                readFile(selectedFile);
                openFileLabel.setText("File Opened: " + selectedFile);
            } catch (IOException ex) {
                Logger.getLogger(ZooSim.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_openFileButtonActionPerformed

    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
        //displayMatrix(ListofLists);
        printInstance();
    }//GEN-LAST:event_printButtonActionPerformed

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        //openZoo(InstanceList.get(0));
        startMaster();
    }//GEN-LAST:event_runButtonActionPerformed
    
    public void startMaster(){
        int masterCount = 0;
        while(allIt.hasNext()){
            Master m = new Master();
            m.start();
            allIt.next();
            masterCount++;            
        }
    }
    public void openZoo(InstanceDay c){
        reset();
        //isRunning = true;
        int count2 = 0;
        setLabels(c);
        textArea.append("Zoo Open!");
            int i = 0;
             // create thread for each customer and start action
            while(i < c.getVisitors()){
                Visitor visitor = new Visitor(i);
                visitor.start();
                i++;
            }

            // initialize the number of gas pumps        
            for (int k = 0; k < c.getPumps(); k++){
                Gas gas = new Gas(k);
                gas.start();
            }
            // initialize the number of cars
            for(int j = 0; j < c.getCars(); j++){
                Car car = new Car(j);
                car.start();

            }
            GasTruck truck = new GasTruck();
            truck.start();

    }
    
    public void reset(){
          carSem = new Semaphore(0);
          visitorSem = new Semaphore(0);
          gasSem = new Semaphore(0);
          needGasSem = new Semaphore(0);
          instanceSem = new Semaphore(0);
          gasTruckSem = new Semaphore(0);
    }
    
    public void openZoo2(){
        //isRunning = true;
        int count2 = 0;
        //while(count2 < 2){
 
            int i = 0;

            // create thread for each customer and start action
            while(i < visitorsActive){
                //textArea.append("\nA Visitor enters." + i);
                Visitor visitor = new Visitor(i);
                visitor.start();
                i++;
                //counter++;
            }
            suspendSleep(1);

            // initialize the number of gas pumps        
            for (int k = 0; k < numGasPumps; k++){
                Gas gas = new Gas(k);
                gas.start();
            }
            // initialize the number of cars
            for(int j = 0; j < numCars; j++){
                Car car = new Car(j);
                car.start();

            }


            
          //  count2++;
        //}
        //System.out.println("Program has finished.");
    }
    
    public void setLabels(InstanceDay a){
        numVisitorsLabel.setText("Initial Visitors: " + a.getVisitors());
        numCarsLabel.setText("Number of Cars: " + a.getCars());
        numPumpsLabel.setText("Number of Pumps: " + a.getPumps());
        carRunTimeLabel.setText("Car Run Time: " + a.getTime());
        instanceLabel.setText("Line Running: "+ (insCount+1));
        gasTruckLabel.setText("Gas truck status: Idle");
    }
    
    public boolean checkInstanceInput(){
            if(allIt.hasNext()){
                allIt.next();
                insCount++;
                return true;
            }
            
            return false;
    }
    
    
    // generates a random number based on maximum and minimum values
    public int randomNum(int max, int min){
        Random r = new Random();
        return r.nextInt(max - min + 1) + min;
    }
    
    // reads in the file based on instances of number of visitors, etc.
    public void readFile(File selected)throws IOException {
        Scanner scan = new Scanner(selected).useDelimiter("(\\s|,)+");
        InstanceDay ins = new InstanceDay();
        int count = 0;
        String temp;
        int a;
        while(scan.hasNext()){
            a = scan.nextInt();
            //StripString(temp);
            //InstanceList.add(temp);
            //InstanceList = new ArrayList<String>();
            //System.out.println("temp: "+ temp);
            //lineCount++;
            
            if(count == 0){
                ins.setVisitors(a);
            }
            else if(count == 1){
                ins.setCars(a);
            }
            else if(count == 2){
                ins.setTime(a);
            }
            else{
                ins.setPumps(a);
            }
            
            count++;
            //if (count >= 3)
            if (count >=4){
                count = 0;
                InstanceList.add(ins);
                ins = new InstanceDay();
                //insCount++;
            }            
        }
        allIt = InstanceList.iterator();
    }
    
    
    public void readFile2(File selected)throws IOException {
        FileReader in = new FileReader(selected);
        
        Scanner scan = new Scanner(selected);
        int c = 0;
        String temp;

        while((c = in.read()) != -1){
            textArea.append("\n" + c);
        }
    }
    
    public void printInstance(){
        Iterator it =  InstanceList.iterator(); 
        int i = 0;
        InstanceDay a = new InstanceDay();
        while(it.hasNext()){
            it.next();
            System.out.println("\nVisitors: " + InstanceList.get(i).getVisitors());
            System.out.println("\nCars: " + InstanceList.get(i).getCars());
            System.out.println("\nTime: " + InstanceList.get(i).getTime());
            System.out.println("\nPumps: " + InstanceList.get(i).getPumps());
            i++;
        }
        
    }
    
    public void displayListInteger(List<Integer> a){
        Iterator iter = a.iterator();
        while(iter.hasNext()){
            
            textArea.append("\n" + iter.next());
        }
    }
    
    public void displayList(List<String> a){
        Iterator iter = a.iterator();
        while(iter.hasNext()){
            
            textArea.append("\n" + iter.next());
        }
    }
    

    public void displayMatrix(List<List<Integer>> a){
        List<Integer> LocalLine = new ArrayList<Integer>();
        for (int j = 0; j < lineCount; j++){
            LocalLine = a.get(j);
            for(int k = 0; k < integerCount; k++){
                textArea.append(LocalLine.get(k) + " ");
            }
            textArea.append("\n");
            LocalLine = new ArrayList<Integer>();
        }
    }
    
    public void suspendSleep(int numSleep){
        try {
                sleep(1000 * numSleep);
            } catch(InterruptedException ex) {
                    System.err.printf("Sleep Error.");
            };
    }
    

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ZooSim.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ZooSim.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ZooSim.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ZooSim.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ZooSim().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel CarsIdleLabel;
    private javax.swing.JLabel VisitorsWaitingLabel;
    private javax.swing.JLabel carRunTimeLabel;
    private javax.swing.JLabel gasRemainingLabel;
    private javax.swing.JLabel gasTruckLabel;
    private javax.swing.JLabel instanceLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel numCarsLabel;
    private javax.swing.JLabel numPumpsLabel;
    private javax.swing.JLabel numVisitorsLabel;
    private javax.swing.JButton openFileButton;
    private javax.swing.JLabel openFileLabel;
    private javax.swing.JButton printButton;
    private javax.swing.JButton runButton;
    private javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables
}
