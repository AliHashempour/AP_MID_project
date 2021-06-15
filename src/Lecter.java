/**
 * The type Lecter.
 *
 * @author ALi.Hashempour
 */
public class Lecter extends Role {

    private int healthBar = 1;

    public void setHealthBar(int healthBar) {
        this.healthBar = healthBar;
    }

    public int getHealthBar() {
        return healthBar;
    }

    @Override
    public void decreaseHealth() {
        healthBar--;
    }

    @Override
    public void increaseHealth() {
        healthBar++;
    }


    public String toString() {
        return "Lecter";
    }
}
