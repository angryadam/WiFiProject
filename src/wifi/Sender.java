package wifi;

import java.util.Random;

import rf.RF;

/**
 *  Thread that takes care of most sending functions of the Link Layer and
 *  implements the entire MAC logic.
 *
 *  @author Adam Myers
 *  @author Kaylene Barber
 */
public class Sender implements Runnable {

    private RF theRF;
    private LinkLayer theLink;
    private int window = RF.aCWmin;
    private Packet packet;

    public Sender(RF theRF, LinkLayer theLink) {
        this.theRF = theRF;
        this.theLink = theLink;
    }

    @Override
    public void run() {

        while (true) {

            try {
                //Sleeps each time so we don't destroy the CPU
                Thread.sleep(10);

                // If there are Packets to be sent in the LinkLayer's outgoing queue block
                // and checks the network for activity
                if (!theLink.getOutgoingBlock().isEmpty()) {
                    int counter = 0;
                    packet = new Packet(theLink.getOutgoingBlock().take().getFrame());

                    boolean receivedACK = false;
                    while (receivedACK == false) {
                        while (counter < RF.dot11RetryLimit) {
                            if (!theRF.inUse()) {
                                // Send the first packet out on the RF layer after SIFS
                                Thread.sleep(RF.aSIFSTime);
                                // ideal case
                                if (!theRF.inUse()) {
                                    theRF.transmit(packet.getFrame());
                                    receivedACK = ackWait();
                                    if (receivedACK) {
                                        break;
                                    } else {

                                        counter++;
                                    }
                                    // Was idle, we waited IFS, then wasn't idle anymore
                                } else {
                                    waitBS();
                                    expBackoff();
                                    theRF.transmit(packet.getFrame());
                                    receivedACK = ackWait();
                                    if (receivedACK) {
                                        break;
                                    } else {

                                        counter++;
                                    }
                                }
                                // was never idle
                            } else {
                                waitBS();
                                expBackoff();
                                theRF.transmit(packet.getFrame());
                                receivedACK = ackWait();
                                if (receivedACK) {
                                    break;
                                } else {

                                    counter++;
                                }
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitBS() {
        // Not busy wait, initial check
        while (theRF.inUse()) {
            try {
                Thread.sleep(RF.aSlotTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // RF is not in use but we wait IFS again
        try {
            Thread.sleep(RF.aSIFSTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // If RF is in use, recurse and uses counter to avoid infinite recurse
        if (theRF.inUse()) {
            waitBS();
        }
    }

    private void expBackoff() {
        int timer = RF.aSlotTime;
        Random random = new Random();

        // while the timer isn't zero
        if (timer != 0) {
            // check rf
            while (theRF.inUse()) {
                try {
                    Thread.sleep(random.nextInt(10));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            timer--;
        } else {
            // timer is zero, must wait one last time
            while (theRF.inUse()) {
                try {
                    Thread.sleep(random.nextInt(10));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean ackWait() {

        try {
            for (int i = 0; i < 10; i++) {
                if (!theLink.receivedACKS.containsKey(packet.getDestAddr())) {
                    // need to change sleep time to be more appropriate??
                    Thread.sleep(10);
                } else {
                    return true;
                }
            }

            if ((window * 2) > RF.aCWmax) {
                window *= 2;
            }
            packet.setRetry(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }
}
