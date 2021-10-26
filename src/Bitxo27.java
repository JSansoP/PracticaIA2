package agents;

// Exemple de Bitxo
import java.util.Random;
import java.time.Instant;

public class Bitxo27 extends Agent {

    static final int PARET = 0;
    static final int BITXO = 1;
    static final int RES = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL = 1;
    static final int DRETA = 2;

    Estat estat;
    Random r = new Random();
    int noMirar = 0;
    int vecesGirado = 0;
    int contadorColision = 0;
    final int distMaxBales = 400;
    int contadorGiro = 100;
    int recursosAnterior = 0;
    boolean vemos = false;

    public Bitxo27(Agents pare) {
        super(pare, "JAJ", "imatges/robotank1.gif");
    }

    @Override
    public void inicia() {
        // atributsAgents(v,w,dv,av,ll,es,hy)
        int cost = atributsAgent(6, 4, 600, 50, 23, 5, 3);
        System.out.println("Cost total:" + cost);

        // Inicialització de variables que utilitzaré al meu comportament
    }
    int girar = 0;

    @Override
    public void avaluaComportament() {
        estat = estatCombat();
        if (recursosAnterior < estat.recursosAgafats) {
            vemos = false;
            recursosAnterior = estat.recursosAgafats;
        }
        camina();
        comidaEnem();
        if (noMirar == 0) {
            mirar();
        } else {
            noMirar--;
        }

        camina();
    }

    /**
     * El método camina s'encarrega del desplaçament del nostre agent a través
     * del entorn, evitant les colisions amb les parets i els enemics, esquivant
     * les esquines i finalment proporciona un moviment controlat i estable
     */
    private void camina() {
        //Miram si esteim en col·lisió 
        if (estat.enCollisio) {
            atura();
            enrere();
            contadorColision++;
            noMirar = 5;
        }
        //Si duim un temps molt baix en col·lisió activam el hiperespai 
        if (contadorColision >= 10) {
            hyperespai();
            contadorColision = 0;
        }
        if(contadorGiro <= 0 && vemos == false){
            giroRecon();
        }
        //Si tenim una paret relativament a prop de noltros comencem a girar cap 
        //a la dreta o esquerra en funcio a la distancia que estroben els visors
        //de la esquerra i de la dreta
        if (hiHaParet(25)) {
            if (estat.objecteVisor[CENTRAL] == PARET) {
                if (estat.distanciaVisors[ESQUERRA] > estat.distanciaVisors[DRETA]) {
                    atura();
                    gira(20 + r.nextInt(20));
                    endavant();
                } else {
                    atura();
                    gira((20 + r.nextInt(20)) * -1);
                    endavant();
                }
                noMirar = 2;

            } else {
                //Si el visor central no veu una paret, pero un dels altres
                //visors veu una paret esquivam aquesta paret fent un petit gir
                //cap a un costat i guardam les vegades que hem girat per aixì
                //després poder continuar amb el mateix sentit
                atura();
                if (estat.distanciaVisors[ESQUERRA] > estat.distanciaVisors[DRETA]) {
                    esquerra();
                    vecesGirado--;
                } else {
                    dreta();
                    vecesGirado++;
                }
                //System.out.println("Girando:" + vecesGirado);
                endavant();
            }
            //Mos tornam a colocar a nes mateix sentit que estàvem abans de 
            //esquivar una paret
        } else if (vecesGirado != 0) {
            atura();
            if (vecesGirado > 0) {
                esquerra();
                vecesGirado--;
                noMirar++;
            } else {
                dreta();
                vecesGirado++;
            }
            //System.out.println("Desgirando:" + vecesGirado);
            endavant();
            //Miram si tenim una paret a una distància llunyana per aixì anar 
            //preparnat el gir cap a la dreta o esquerra en funcio a les distàncies
            //dels visors
        } else if (hiHaParet(85)) {
            if (estat.distanciaVisors[CENTRAL] < 85) {
                if (estat.distanciaVisors[ESQUERRA] > estat.distanciaVisors[DRETA]) {
                    atura();
                    esquerra();
                    endavant();
                } else {
                    atura();
                    dreta();
                    endavant();
                }

            } else {
                atura();
                endavant();
            }

        } else {
            atura();
            endavant();
        }

        contadorGiro--;
        System.out.println(contadorGiro);
    }

    /**
     * Métode que s'encarrega de cercar els recursos enemics dins l'entern
     *
     * @return
     */
    private void comidaEnem() {
        Objecte fin = null;
        int distMin = 9999999;
        if (estat.veigAlgunRecurs) {
            for (int i = 0; i < estat.numObjectes; i++) { //Recorrem tots els recursos
                Objecte aux = estat.objectes[i];
                if (aux.agafaTipus() >= 100 && aux.agafaTipus() != (100 + estat.id) && (aux.agafaSector() == 2 || aux.agafaSector() == 3) && aux.agafaDistancia() < distMaxBales) {
                    if (distMin > aux.agafaDistancia()) {
                        distMin = aux.agafaDistancia();
                        fin = aux;
                    }
                }
            }
        }
        if (fin != null && estat.llançaments > 0 && !estat.llançant) {
            mira(fin);
            llança();
        }
    }

    /**
     * Métode que s'encarrega de cercar els nostres recursos i els escuts dins
     * l'entorn
     */
    private void mirar() {
        Objecte fin = null;
        int distMin = 9999999;
        if (estat.veigAlgunRecurs || estat.veigAlgunEscut) {
            for (int i = 0; i < estat.numObjectes; i++) { //Recorrem tots els recursos
                Objecte aux = estat.objectes[i];
                if (aux.agafaTipus() == 100 + estat.id || aux.agafaTipus() == Estat.ESCUT) {
                    if (distMin > aux.agafaDistancia()) {
                        distMin = aux.agafaDistancia();
                        fin = aux;
                    }
                }
            }
        }
        if (fin != null) {
            mira(fin);
            vemos = true;
        }

    }
    /**
     * Método que se encarga de hacer un giro de reconocimiento
     */
    public void giroRecon() {
        atura();
        gira(180);
        endavant();
        contadorGiro = 100;
        System.out.println("Giramos");
    }

    /**
     * Métode que s'encarrega de retornar si el nostre agent veu una paret a una
     * distància que reb per paràmetre
     *
     * @param distancia
     * @return
     */
    private boolean hiHaParet(int distancia) {

        return (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] < distancia)
                || (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] < distancia)
                || (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < distancia);
    }

}
