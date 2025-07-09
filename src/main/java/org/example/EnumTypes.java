package org.example;

public final class EnumTypes {

    // Costruttore privato per evitare l'istanza della classe
    private EnumTypes() {
        throw new UnsupportedOperationException("Utility class");
    }

    public enum EnumStatoContratto {
        ATTIVO,   // Contratto attivo
        SCADUTO,
        TERMINATO,// Contratto scaduto
        NESSUNRECORD,
        AMBIGUO,
        ULTIMADATASCADUTA,
        CROSSCOMPAGNIA,
        SCARTIDARECUPERARE,
        SOSPESO,
        DASTORNARE,
        STORNATO,
        IDULTIMOMOVERRATO,
        ERRORSQL// Situazione non gestita
    }

    public enum EnumStatoPeriferica {
        ATTIVA,
        ABBINATA,
        DISATTIVA,
        DISINSTALLATA,
        NOLASTRECORD,
        PERIFERICASUDIVERSOCONTRATTO,
        NESSUNAPERIFERICA,
        SANAREEXT,
        DOPPIAA,
        NODATE,
        NODATADIS,
        AMBIGUA,
        DADISATTIVARE,
        STATONONVALIDO,
        NESSUNADATA,
        STATOANOULTIMORECORD,
        ERRORSQL
    }

    public enum EnumComagnia {
        MES,
        JENIOT
    }

    public enum EnumTipoMovimento {
        EMISSIONE,
        MODIFICA,
        RINNOVO,
        STORNO
    }

    public enum EnumStatoPolizza {
        EMISSIONE,
        MODIFICA,
        RINNOVO,
        STORNO,
        INCORSO,
        RIAMMISSIONE,
        CASENONIDENTIFICATO,
        SOSPESA,
        AMBIGUO
    }
}
