package com.example.medical_iot;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.medical_iot.model.ArchiveDataModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MedicalIOTUnitTest {
    @Test
    public void testConstructorWithoutParam()
    {
        ArchiveDataModel archiveTest = new ArchiveDataModel();
        assertEquals(0,archiveTest.getID_chambre());
        assertFalse(archiveTest.getDeplacement_surveillant());
        assertFalse(archiveTest.getAcquittement_surveillant());
        assertEquals("00:00", archiveTest.getHeure_acquittement());
        assertEquals(" ", archiveTest.getNom_surveillant());
        assertEquals(" ", archiveTest.getPrenom_surveillant());
        assertEquals(" ", archiveTest.getEspace_commentaire());
    }

    @Test
    public void testConstructorWithParam()
    {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.FRANCE);
        String hour = dateFormat.format(date);

        ArchiveDataModel archiveTest = new ArchiveDataModel(1, true, true, hour, "DeNice","Brice","bonjour les programmeurs");
        assertEquals(1,archiveTest.getID_chambre());
        assertTrue(archiveTest.getDeplacement_surveillant());
        assertTrue(archiveTest.getAcquittement_surveillant());
        assertEquals(hour, archiveTest.getHeure_acquittement());
        assertEquals("DeNice", archiveTest.getNom_surveillant());
        assertEquals("Brice", archiveTest.getPrenom_surveillant());
        assertEquals("bonjour les programmeurs", archiveTest.getEspace_commentaire());
    }

    @Test
    public void testSetter()
    {
        ArchiveDataModel archive = new ArchiveDataModel();
        archive.setAcquittement_surveillant(true);
        archive.setDeplacement_surveillant(true);
        archive.setHeure_acquittement("23:09");
        archive.setNom_surveillant("Bon");
        archive.setPrenom_surveillant("Jean");
        archive.setEspace_commentaire("salut");

        assertTrue(archive.getDeplacement_surveillant());
        assertTrue(archive.getAcquittement_surveillant());
        assertEquals("23:09", archive.getHeure_acquittement());
        assertEquals("Bon", archive.getNom_surveillant());
        assertEquals("Jean", archive.getPrenom_surveillant());
        assertEquals("salut", archive.getEspace_commentaire());
    }

    //il faut s'assurer mnt que les valeurs collectées soient conforme a ce qui est demandé

    //@Test
    /*public void validationHourTest()
    {
        String hour1 = "10:90"; //faux
        String hour2 = "10:20"; //juste
        String hour3 = "ok"; //faux
        String hour4 = "09:10"; //juste
        String hour5 = "7:8"; //faux
        String hour6 = "07:890"; //faux

        assertFalse(validateHourFormat(hour1));
        assertTrue(validateHourFormat(hour2));
        assertFalse(validateHourFormat(hour3));
        assertTrue(validateHourFormat(hour4));
        assertFalse(validateHourFormat(hour5));
        assertFalse(validateHourFormat(hour6));
    }

    private boolean validateHourFormat(String hour) {
        // copie de la méthode de la classe ArchiveDataModel
        String heurePattern = "^([01]\\d|2[0-3]):([0-5]\\d)$";
        Pattern pattern = Pattern.compile(heurePattern);
        Matcher matcher = pattern.matcher(hour);
        return matcher.matches();
    }*/
    
    //mnt on teste avec la classe ArchiveDataModel

    @Test(expected = IllegalArgumentException.class)
    public void testFormatHeure()
    {
        ArchiveDataModel archive1 = new ArchiveDataModel(1, true, false, "10:90", "o", "b", "ok");
        ArchiveDataModel archive2 = new ArchiveDataModel(1, true, false, "10:20", "o", "b", "ok");
        ArchiveDataModel archive3 = new ArchiveDataModel(1, true, false, "ok", "o", "b", "ok");
        ArchiveDataModel archive4 = new ArchiveDataModel(1, true, false, "09:10", "o", "b", "ok");
        ArchiveDataModel archive5 = new ArchiveDataModel(1, true, false, "7:8", "o", "b", "ok");
        ArchiveDataModel archive6 = new ArchiveDataModel(1, true, false, "37:890", "o", "b", "ok");
    }


}