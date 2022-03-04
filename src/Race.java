import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class Race {
    //Парковочное место занято - true, свободно - false
    private static final boolean[] TUNNEL_PLACES = new boolean[3];
    //Устанавливаем флаг "справедливый", в таком случае метод
    //aсquire() будет раздавать разрешения в порядке очереди
    private static final Semaphore SEMAPHORE = new Semaphore(3, true);
    //Создаем CountDownLatch на 8 "условий"
    private static final CountDownLatch START = new CountDownLatch(13);
    //Условная длина гоночной трассы
    private static final int trackLength = 5000;
    private static final int tunnelLength = 2500;
    //Время прохождения гоночной трассы
    private static long startTime = 0L;
    private static long elapsedTime = 0L;

    public static void main(String[] args) throws InterruptedException {
        ArrayList<Car> racingCars = new ArrayList<>();
        Map<String, Long> resultTable = new HashMap<>();

        Car first = new Car(95, "Koenigsegg", "Agera R", getRandomRoadSpeed(), getRandomCarPreparationTime(), ConsoleColors.RED_BRIGHT);
        Car second = new Car(86, "Ford", "GT40", getRandomRoadSpeed(), getRandomCarPreparationTime(), ConsoleColors.GREEN);
        Car third = new Car(66, "Dodge", "Viper", getRandomRoadSpeed(), getRandomCarPreparationTime(), ConsoleColors.YELLOW_BRIGHT);
        Car fourth = new Car(54, "Porsche", "911", getRandomRoadSpeed(), getRandomCarPreparationTime(), ConsoleColors.BLUE);
        Car fifth = new Car(43, "McLaren", "F1", getRandomRoadSpeed(), getRandomCarPreparationTime(), ConsoleColors.PURPLE_BRIGHT);
        Car sixth = new Car(33, "Ferrari", "FF", getRandomRoadSpeed(), getRandomCarPreparationTime(), ConsoleColors.CYAN_BRIGHT);
        Car seventh = new Car(31, "Bugatti", "Veyron", getRandomRoadSpeed(), getRandomCarPreparationTime(), ConsoleColors.WHITE_BRIGHT);
        Car eighth = new Car(24, "Lamborghini", "Aventador", getRandomRoadSpeed(), getRandomCarPreparationTime(), ConsoleColors.BLACK);
        Car ninth = new Car(20, "Pagani", "Zonda", getRandomRoadSpeed(), getRandomCarPreparationTime(), ConsoleColors.GREEN_BRIGHT);
        Car tenth = new Car(13, "Maserati", "Birdcage", getRandomRoadSpeed(), getRandomCarPreparationTime(), ConsoleColors.BLUE_BRIGHT);

        racingCars.add(first);
        racingCars.add(second);
        racingCars.add(third);
        racingCars.add(fourth);
        racingCars.add(fifth);
        racingCars.add(sixth);
        racingCars.add(seventh);
        racingCars.add(eighth);
        racingCars.add(ninth);
        racingCars.add(tenth);

        Car carWithTheLargestPreparationTime = racingCars.stream().max(Comparator.comparing(Car::getPreparationTimeMS)).orElseThrow(NoSuchElementException::new);
        System.out.println("The race will start when all cars will be ready. Car with the longest preparation time: " + carWithTheLargestPreparationTime.toString() + " - " + (double)carWithTheLargestPreparationTime.getPreparationTimeMS()/1000 + " sec.");
        Thread.sleep(carWithTheLargestPreparationTime.getPreparationTimeMS());

        ThreadGroup threadGroup = new ThreadGroup("Race");
        for (Car car: racingCars) {
            Thread thread = new Thread(threadGroup, car);
            thread.start();
        }

        while (START.getCount() > 6) //Проверяем, собрались ли все автомобили
            Thread.sleep(100);              //у стартовой прямой. Если нет, ждем 100ms

        Thread.sleep(1000);
        System.out.println("Ready!");
        START.countDown();//Команда дана, уменьшаем счетчик на 1
        Thread.sleep(1000);
        System.out.println("Steady!");
        START.countDown();//Команда дана, уменьшаем счетчик на 1
        Thread.sleep(1000);
        System.out.println("Go!");
        START.countDown();//Команда дана, уменьшаем счетчик на 1
        //счетчик становится равным нулю, и все ожидающие потоки
        //одновременно разблокируются

        while (threadGroup.activeCount() > 0){
            Thread.sleep(100);
        }
        resultTable.put(first.toString(), first.getElapsedTime());
        resultTable.put(second.toString(), second.getElapsedTime());
        resultTable.put(third.toString(), third.getElapsedTime());
        resultTable.put(fourth.toString(), fourth.getElapsedTime());
        resultTable.put(fifth.toString(), fifth.getElapsedTime());
        resultTable.put(sixth.toString(), sixth.getElapsedTime());
        resultTable.put(seventh.toString(), seventh.getElapsedTime());
        resultTable.put(eighth.toString(), eighth.getElapsedTime());
        resultTable.put(ninth.toString(), ninth.getElapsedTime());
        resultTable.put(tenth.toString(), tenth.getElapsedTime());

        List<Map.Entry<String, Long>> sortedResultTable = new ArrayList<>(resultTable.entrySet());
        sortedResultTable.sort(Map.Entry.comparingByValue());
        System.out.println("\nResult table:");
        for (int i = 0; i < sortedResultTable.size(); i++){
            System.out.println(i+1 + "." + sortedResultTable.get(i).getKey() + " - " + (double)sortedResultTable.get(i).getValue()/1000000000 + " sec.");
        }
    }

    public static int getRandomRoadSpeed() {
        return (int) ((Math.random() * (320 - 240)) + 240);
    }
    public static long getRandomCarPreparationTime(){
        return (long) ((Math.random() * (10000 - 1000)) + 1000);
    }

    public static class Car implements Runnable {
        private int number;
        private String make;
        private String model;
        private int speed;
        private long preparationTimeMS;
        private String color;

        public long getPreparationTimeMS() {
            return preparationTimeMS;
        }

        public void setPreparationTimeMS(long preparationTimeMS) {
            this.preparationTimeMS = preparationTimeMS;
        }

        public long getElapsedTime() {
            return elapsedTime;
        }

        public void setElapsedTime(long elapsedTime) {
            this.elapsedTime = elapsedTime;
        }

        private long elapsedTime;

        @Override
        public String toString() {
            return "Car " + number +
                    " " + make +
                    " " + model;
        }

        public Car(int number, String make, String model, int speed, long preparationTimeMS, String color){
            this.number = number;
            this.make = make;
            this.model = model;
            this.speed = speed;
            this.preparationTimeMS = preparationTimeMS;
            this.color = color;
        }

        @Override
        public void run() {
            try {
                System.out.println(color + "Car" + toString() + " has arrived to the start line.\n" + ConsoleColors.RESET);
                //Автомобиль подъехал к стартовой прямой - условие выполнено
                //уменьшаем счетчик на 1
                START.countDown();
                //метод await() блокирует поток, вызвавший его, до тех пор, пока
                //счетчик CountDownLatch не станет равен 0
                START.await();
                startTime = System.nanoTime();
                Thread.sleep(trackLength / speed);//ждем пока проедет трассу
                System.out.println(color + "Car" + toString() + " has arrived to the tunnel!\n");
                //acquire() запрашивает доступ к следующему за вызовом этого метода блоку кода,
                //если доступ не разрешен, поток вызвавший этот метод блокируется до тех пор,
                //пока семафор не разрешит доступ
                SEMAPHORE.acquire();

                int tunnelNumber = -1;

                //Ищем свободное место и паркуемся
                synchronized (TUNNEL_PLACES){
                    for (int i = 0; i < 5; i++)
                        if (!TUNNEL_PLACES[i]) {      //Если место свободно
                            TUNNEL_PLACES[i] = true;  //занимаем его
                            tunnelNumber = i;         //Наличие свободного места, гарантирует семафор
                            System.out.println(color + "Car" + toString() + " has entered the tunnel\n");
                            break;
                        }
                }

                Thread.sleep(5000);

                synchronized (TUNNEL_PLACES) {
                    TUNNEL_PLACES[tunnelNumber] = false;//Освобождаем место
                }

                //release(), напротив, освобождает ресурс
                SEMAPHORE.release();
                System.out.println(color + "Car" + toString() + " has passed the tunnel\n");
                Thread.sleep(trackLength / speed);
                System.out.println(color + "Car" + toString() + " has finished!.\n" + ConsoleColors.RESET);
                long elapsedTime = (System.nanoTime() - startTime);
                setElapsedTime(elapsedTime);
            } catch (InterruptedException e) {
            }
        }
    }

    public static class ConsoleColors { //класс с цветами для того, чтобы различать объекты в консоли
        public static final String RESET = "\033[0m";  // Text Reset

        public static final String BLACK = "\033[0;30m";   // BLACK
        public static final String BLUE = "\033[0;34m";    // BLUE
        public static final String GREEN = "\033[0;32m";   // GREEN

        public static final String RED_BRIGHT = "\033[0;91m";    // RED
        public static final String GREEN_BRIGHT = "\033[0;92m";  // GREEN
        public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
        public static final String BLUE_BRIGHT = "\033[0;94m";   // BLUE
        public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
        public static final String CYAN_BRIGHT = "\033[0;96m";   // CYAN
        public static final String WHITE_BRIGHT = "\033[0;97m";  // WHITE
    }
}
