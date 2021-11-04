package agents;

// Exemple de Bitxo
import java.util.Random;

public class Bitxo26 extends Agent {

    static final int PARET = 0;
    static final int BITXO = 1;
    static final int RES = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL = 1;
    static final int DRETA = 2;

    //Declaració de variables 
    
    final int distMaxBales = 400;
    final int distMaxEnemic = 120;
    final int resetGiro = 120;
    Estat estat;
    Random r = new Random();
    int comptadorGir = 120;
    int noMirar = 0;
    int comptadorColisio = 0;
    int impactesAnterior = 0;
    int comptadorDispar = 0;

    public Bitxo26(Agents pare) {
        super(pare, "Exemple26", "imatges/hipnotizar.gif");
    }

    @Override
    public void inicia() {
        // atributsAgents(v,w,dv,av,ll,es,hy)
        int cost = atributsAgent(6, 5, 600, 45, 23, 5, 3);
        System.out.println("Cost total:" + cost);
        comptadorDispar = 0;
        impactesAnterior = 0;
        comptadorColisio = 0;
        noMirar = 0;
        comptadorGir = 120;
        // Inicialització de variables que utilitzaré al meu comportament
    }

    @Override
    public void avaluaComportament() {
        estat = estatCombat();
        atura();
        comptadorDispar--;
        Objecte fin = evaluarDisparMenjar();
        Objecte fin2 = evaluarDisparEnemic();
        if (fin2 != null && comptadorDispar < 0) {
            comptadorDispar = 60;
            mira(fin2);
            llança();
        } else if (fin != null) {
            mira(fin);
            llança();
            System.out.println("miramosrecurso");
        }
        if (noMirar <= 0 && fin2 == null) {
            miraRecurs();
        } else {
            noMirar--;
        }
        camina();
    }
    
    /**
    * El mètode camina s'encarrega del desplaçament del nostre agent a través
    * de l'entorn, comprovant en primer lloc si estem en col·lisió sinó
    * en funció de la distància que estiguem a les parets el nostre agent girarà
    * més o menys, després si passa un temps en què l'agent no veu cap recurs
    * llavors fa un gir de 120 º aleatori per tornar a avaluar l'entorn
    * i finalment en cada iteració gestionem les accions que realitza el
    * agent sobre un agent enemic
    */
    
    private void camina() {
        //Estamos en colisión
        if (estat.enCollisio) {
            atura();
            comptadorColisio++;
            gestionaColisio();
        } else {
            comptadorColisio = 0;
            //Hay una pared cerca
            if (hiHaParet(30)) {
                giraAProp();

            } else if (hiHaParet(85) && estat.distanciaVisors[CENTRAL] < 90) {    //Hay una pared relativamente lejos
                giraLluny();

            } else if (comptadorGir <= 0) {
                girRecon();
            } else {
                //System.out.println("endavant");
                endavant();
            }
            gestionarEnemic();
        }
    }

    /**
    * Aquest mètode s'encarrega de:
    * En primer lloc, detectar si un dels nostres sectors observa un tret
    * enemic i si no tenim l'escut activat un cop s'hagi complert aquesta
    * condició verifiquem si el llançament s'encunetra a prop nostre si
    * és així activem l'escut.
    * En segon lloc, detectem si l'enemic ens ha impacatat un tret,
    * simplement mirant el nombre de recursos que tenim, si és així activem
    * l'escut.
    *
    */
    
    private void gestionarEnemic() {
        if (estat.llançamentEnemicDetectat && !estat.escutActivat) {
            if (estat.distanciaLlançamentEnemic < 40) {
                activaEscut();
                noMirar = 5; //QUITAMOS?
            }
            endavant();
        }
        if (impactesAnterior < estat.impactesRebuts) {
            if (!estat.escutActivat) {
                activaEscut();
            }
        }
        impactesAnterior = estat.impactesRebuts;

    }
    
    /**
    * Aquest mètode s'encarrega de gestionar les accions que realitza l'agent
    * en col·lisió, primer, duem a terme la verificació d'un comptador
    * de col·lisió, és a dir, si el nostre agent porta força temps
    * col·lisionant activem l'hiperespai, sinó intentem evitar la col·lisió,
    * realitzant un gir i un moviment aleatori.
    */

    private void gestionaColisio() {
        if (comptadorColisio > 50) {
            hyperespai();
        } else {
            boolean a = r.nextBoolean();
            if (a) {
                esquerra();
            } else {
                dreta();
            }

            a = r.nextBoolean();
            if (a) {
                endavant();
            } else {
                enrere();

            }
            //noMirar = 5;
        }
    }
    
    /**
    * Aquest mètode s'encarrega de:
    * Cercar els nostres recursos dins de l'entorn de manera prioritària, però
    * si veiem algun escut i en tenim pocs o si no veiem cap recurs a prop
    * anem a cercar els escuts.
    * Mentre no veiem res durant un temps anem decrementant el comptador
    * de gira per així fer un gir i tornar a avaluar l'entorn.
    */

    private void miraRecurs() { //Falta evaluar los escudos
        Objecte fin = null;
        int distMin = 99999999;
        if (estat.veigAlgunRecurs) {
            for (int i = 0; i < estat.numObjectes; i++) { //Recorrem tots els recursos
                Objecte aux = estat.objectes[i];
                if (aux.agafaTipus() == 100 + estat.id) {
                    if (distMin > aux.agafaDistancia()) {
                        distMin = aux.agafaDistancia();
                        fin = aux;
                    }
                }
            }
        }
        if ((estat.veigAlgunEscut && estat.escuts < 5) || !estat.veigAlgunRecurs) {
            for (int i = 0; i < estat.numObjectes; i++) { //Recorrem tots els recursos
                Objecte aux = estat.objectes[i];
                if (aux.agafaTipus() == Estat.ESCUT) {
                    if (distMin > aux.agafaDistancia()) {
                        distMin = aux.agafaDistancia();
                        fin = aux;
                    }
                }
            }
        }

        if (fin != null) {
            mira(fin);
        } else if (!estat.veigAlgunRecurs && estat.veigAlgunEscut) {
            comptadorGir--;
        }
    }

    /**
     * Mètode que s'encarrega de retornar un recurs enemic
     * @return 
     */
    
    private Objecte evaluarDisparMenjar() {
        Objecte fin = null;
        int distMin = 9999999;
        if (!estat.llançant && estat.veigAlgunRecurs) {
            for (int i = 0; i < estat.numObjectes; i++) { //Recorrem tots els objectes
                Objecte aux = estat.objectes[i];
                if (aux.agafaTipus() != Estat.AGENT && ((aux.agafaTipus() >= 100 && aux.agafaTipus() != (100 + estat.id)) && (aux.agafaSector() == 2 || aux.agafaSector() == 3) && aux.agafaDistancia() < distMaxBales)) {
                    if (distMin > aux.agafaDistancia()) {
                        distMin = aux.agafaDistancia();
                        fin = aux;
                    }
                }
            }
        }
        return fin;
    }
    
    /**
     * Mètode que s'encarrega de retornar si hi ha algun enemic per llançar
     * @return 
     */
    
    private Objecte evaluarDisparEnemic() {
        Objecte fin = null;
        int distMin = 9999999;
        if (!estat.llançant && (estat.veigAlgunRecurs || estat.veigAlgunEnemic)) {
            for (int i = 0; i < estat.numObjectes; i++) { //Recorrem tots els objectes
                Objecte aux = estat.objectes[i];
                
                if ((aux.agafaTipus() == Estat.AGENT) && (aux.agafaSector() == 2 || aux.agafaSector() == 3) && aux.agafaDistancia() < distMaxBales && aux.agafaDistancia() <= distMaxEnemic) {
                    fin = aux;
                    return fin;
                }
            }
        }
        return fin;
        /*if (fin != null && estat.llançaments > 0 && !estat.llançant) {
            if (fin.agafaTipus() == Estat.AGENT && fin.agafaDistancia() <= 100) {
                mira(fin);
                llança();

            } else if (fin.agafaTipus() != Estat.AGENT) {
                mira(fin);
                llança();
            }

        }*/
    }
    
    /**
    * Mètode que s'encarrega de girar uns graus entre [10º - 40º], pero si el 
    * visor central veu una paret a una distancia menor a 70 px aleshores fem 
    * un gir aleatori dins el range sino vol dir que te una paret lluny, 
    * alsehores feim un gir de 10º i en funció a la distància que es troben els 
    * visors de l'esquerra o dreta. Si el visor de l'esquerra és més gran a la 
    * dreta girem cap a l'esquerra en cas contrari girem cap a la dreta.
    */
    
    private void giraAProp() {
        if (estat.distanciaVisors[ESQUERRA] > estat.distanciaVisors[DRETA]) {
            atura();
            gira(10 + r.nextInt(30)*(estat.distanciaVisors[CENTRAL]<70 ? 1:0));
            endavant();
        } else {
            atura();
            gira(-1 * (10 + r.nextInt(30)*(estat.distanciaVisors[CENTRAL]<70 ? 1:0)));
            endavant();
        }
    }
    
    /**
    * Mètode que s'encarrega de girar en funció a la distància que es troben 
    * els visors de l'esquerra o dreta. Si el visor de l'esquerra és més gran 
    * a la dreta girem a l'esquerra en cas contrari girem a la dreta
    */
    
    private void giraLluny() {
        if (estat.distanciaVisors[ESQUERRA] > estat.distanciaVisors[DRETA]) {
            atura();
            esquerra();
            endavant();
        } else {
            atura();
            dreta();
            endavant();
        }
    }

    /**
    * Mètode que s'encarrega de fer un gir de 120º a l'esquerra o dreta
    * de forma aleatòria
    */
    
    private void girRecon() {
        atura();
        if (r.nextBoolean()) {
            gira(120);
        } else {
            gira(-120);
        }
        endavant();
        comptadorGir = resetGiro;
    }

    /**
     * Método que se encarga de devolver si nuestro agente ve una paret a una 
     * distáncia que recibe por parámetro.
     * @param distancia
     * @return 
     */

    private boolean hiHaParet(int distancia) {
        return (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] < distancia)
                || (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] < distancia)
                || (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < distancia);
    }
}
