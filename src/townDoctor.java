/**
 * The type Town doctor.
 *
 * @author ALi.Hashempour
 */
public class townDoctor extends Role {

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
        return "townDoctor";
    }
}
