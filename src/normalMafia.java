/**
 * The type Normal mafia.
 *
 * @author ALi.Hashempour
 */
public class normalMafia extends Role {

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
        return "normalMafia";
    }
}
