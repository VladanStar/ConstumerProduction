import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class ConsumerProducer {
    private static Buffer buffer = new Buffer();

    public static void main(String[] args) {
        // Kreiranje pula niti sa dve niti
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(new ProducerTask());
        executor.execute(new ConsumerTask());
        executor.shutdown();
    }

    // Zadataj dodavanja int vrednosti u bafer
    private static class ProducerTask implements Runnable {
        public void run() {
            try {
                int i = 1;
                while (true) {
                    System.out.println("Producer writes " + i);
                    buffer.write(i++); // Add a value to the buffer
                    // Put the thread into sleep
                    Thread.sleep((int)(Math.random() * 10000));
                }
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Zadatak čitanja i brisanja int vrednosti iz bafera
    private static class ConsumerTask implements Runnable {
        public void run() {
            try {
                while (true) {
                    System.out.println("\t\t\tConsumer reads " + buffer.read());
                    // Put the thread into sleep
                    Thread.sleep((int)(Math.random() * 10000));
                }
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Unutrašnja klasa za bafer
    private static class Buffer {
        private static final int CAPACITY = 1; // buffer size
        private java.util.LinkedList<Integer> queue =
                new java.util.LinkedList<>();

        // Kreiranje novog kluča
        private static Lock lock = new ReentrantLock();

        // Kreiranje dva uslova
        private static Condition notEmpty = lock.newCondition();
        private static Condition notFull = lock.newCondition();

        public void write(int value) {
            lock.lock(); // Acquire the lock
            try {
                while (queue.size() == CAPACITY) {
                    System.out.println("Wait for notFull condition");
                    notFull.await();
                }

                queue.offer(value);
                notEmpty.signal(); // SSignal zanotEmpty uslov
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            finally {
                lock.unlock(); // Oslobađanje ključa
            }
        }

        public int read() {
            int value = 0;
            lock.lock(); // Zahtev za ključem
            try {
                while (queue.isEmpty()) {
                    System.out.println("\t\t\tWait for notEmpty condition");
                    notEmpty.await();
                }

                value = queue.remove();
                notFull.signal(); // Signal za notFull uslov
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            finally {
                lock.unlock(); // Oslobađaanje ključa
                return value;
            }
        }
    }
}