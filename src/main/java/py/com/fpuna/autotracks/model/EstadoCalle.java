/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package py.com.fpuna.autotracks.model;

/**
 *
 * @author Ruben
 */
public class EstadoCalle {
    
    private Asu2po4pgr way;
    private long count;

    public EstadoCalle() {
    }

    public EstadoCalle(Asu2po4pgr way, long count) {
        this.way = way;
        this.count = count;
    }

    public Asu2po4pgr getWay() {
        return way;
    }

    public void setWay(Asu2po4pgr way) {
        this.way = way;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

}
