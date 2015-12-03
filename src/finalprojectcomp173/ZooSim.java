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
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;


public class ZooSim extends javax.swing.JFrame {

    private File selectedFile;
    private String sFile;
    private JFileChooser fc = new JFileChooser();
    
    private int numVisitors = 50;
    private int numCars = 5;
    private int numGasPumps = 3;
    private int waitTime;
    private int visitorsPresent = 0;
    private Semaphore carSem = new Semaphore(0);
    private Semaphore visitorSem = new Semaphore(0);
    private Semaphore gasSem = new Semaphore(0);
    private Semaphore needGasSem = new Semaphore(0);
    private int integerCount = 4;
    private int lineCount;
    private int allGas = 200;
    
    public Car car = new Car();
    public Visitor visitor = new Visitor();
    
    private int sleepSeconds = 1;

    private List<String> InstanceList;
    private List<Integer> InstanceListInteger = new ArrayList<Integer>();
    private List<List<Integer>> ListofLists = new ArrayList<List<Integer>>();
    
    
    
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

            // if there are no customers in shop barber sleeps
            //if(visitorsPresent <= 0)
                        //barberStatus.setText("Barber Status: sleep");
                // cars idle
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
            numRides = 5;
        }
        
        public void drive(){
            numRides--;
            suspendSleep(1);
        }
        public void getGas(){
            needGasSem.release(1);
            try{
                gasSem.acquire(1);
                textArea.append("Car " + CarNum + " got gas.");
                numRides = 5;
            }
            catch (InterruptedException e){
                        System.err.printf("Error on lock");
            }
        }
        public void startCar(){
            textArea.append("\nCar " + CarNum + "Started");
            while(visitorsPresent > 0){
                CarsIdleLabel.setText("Cars at Idle: waiting...");
                // if number of rides ==0 get gas
                if (numRides <= 0){
                    getGas();
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
            VisitorsWaitingLabel.setText("Visitors Waiting: " + 0);
        }
        
        
        public void run(){
            startCar();
            // add a car based on loop
            carSem.release(1);
            textArea.append("\nCar " + CarNum+ " Started.");
        }
    }
    
    public class Master extends Thread{
        public void run(){
            
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
            suspendSleep(15);
            allGas = 200;
        }
        
        public void openGasStation(){
            textArea.append("\nGas Pump " + pumpNumber+ " on");
            
            
            while(numCars > 0){
                System.out.println("waiting....");
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
                
                if(allGas < 50){
                    refillStation();
                    textArea.append("Refilling Station");
                    gasRemainingLabel.setText("Gas Remaining: " + allGas);
                }
            }
            
        }
        public void run(){
            gasRemainingLabel.setText("Gas Remaining: " + allGas);
            openGasStation();
            gasSem.release(1);
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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(VisitorsWaitingLabel)
                    .addComponent(CarsIdleLabel)
                    .addComponent(gasRemainingLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(VisitorsWaitingLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(CarsIdleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(gasRemainingLabel)
                .addContainerGap(65, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(printButton)
                .addContainerGap())
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
        displayMatrix(ListofLists);
    }//GEN-LAST:event_printButtonActionPerformed

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        // TODO add your handling code here:
        openZoo();
    }//GEN-LAST:event_runButtonActionPerformed
    
    public void openZoo(){
        int i = 0;
        // start new thread for car
        //car.start();
        
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
        
        
        // create thread for each customer and start action
        while(i < numVisitors){
            //textArea.append("\nA Visitor enters." + i);
            Visitor visitor = new Visitor(i);
            visitor.start();
            i++;
            //counter++;
        }
        
    }
    
    // reads in the file based on instances of number of visitors, etc.
    public void readFile(File selected)throws IOException {
        Scanner scan = new Scanner(selected).useDelimiter("(\\s|,)+");
        InstanceList = new ArrayList<String>();
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
            count++;
            textArea.append("\n"  + a);
            
            // when it is done reading a line.
            if (count >= 4){
                InstanceList = new ArrayList<String>();
                count = 0;
                break;
            }
        }
    }
    
    
    public void readFile2(File selected)throws IOException {
        FileReader in = new FileReader(selected);
        
        Scanner scan = new Scanner(selected);
        InstanceList = new ArrayList<String>();
        int c = 0;
        String temp;

        while((c = in.read()) != -1){
            textArea.append("\n" + c);
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
    
    public void StripString(String test){
        int i = 0;
        char temp;
        InstanceListInteger = new ArrayList<Integer>();
        StringBuilder aString = new StringBuilder();
        String into;
        while(i < test.length()){
            temp = test.charAt(i);
            System.out.print(temp);
            if(Character.isDigit(temp))
                aString.append(temp);
            else if(temp == ',' || (i-1) == test.length()){
                //integerCount++;
                into = aString.toString();
                InstanceListInteger.add(Integer.parseInt(into));
            }
                
                
            i++;
        }
        ListofLists.add(InstanceListInteger);
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
    private javax.swing.JLabel gasRemainingLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton openFileButton;
    private javax.swing.JLabel openFileLabel;
    private javax.swing.JButton printButton;
    private javax.swing.JButton runButton;
    private javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables
}
