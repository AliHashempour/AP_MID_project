public class godFather extends Role {

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

    @Override
    public void action() {

    }

    public String toString() {
        return "godFather";
    }
}
