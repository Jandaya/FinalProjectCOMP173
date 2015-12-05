/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finalprojectcomp173;

/**
 *
 * @author Joseph
 */
public class InstanceDay {
    private int visitors;
    private int cars;
    private int time;
    private int pumps;
    public InstanceDay(){
        visitors = 0;
        cars = 0;
        time = 0;
        pumps = 0;
    }
    
    public void setVisitors(int v){
        visitors = v;
    }
    public void setCars(int c){
        cars = c;
    }
    public void setTime(int t){
        time = t;
    }
    public void setPumps(int p){
        pumps = p;
    }
    
    public int getVisitors(){
        return visitors;
    }
    public int getCars(){
        return cars;
    }
    public int getTime(){
        return time;
    }
    public int getPumps(){
        return pumps;
    }
    
    
    
}
