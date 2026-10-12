package com.ammaia_ispc.sangreyamobile.tests;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.activities.LoginActivity;
import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.CampaignHelper;
import com.ammaia_ispc.sangreyamobile.helpers.SessionManager;
import com.ammaia_ispc.sangreyamobile.screens.AdminCampaignListScreen;
import com.ammaia_ispc.sangreyamobile.screens.AdminDashboardScreen;
import com.ammaia_ispc.sangreyamobile.screens.CampaignDetailScreen;
import com.ammaia_ispc.sangreyamobile.screens.CampaignFormScreen;
import com.ammaia_ispc.sangreyamobile.screens.CampaignListScreen;
import com.ammaia_ispc.sangreyamobile.screens.LoginScreen;
import com.ammaia_ispc.sangreyamobile.screens.MyEnrollmentsScreen;
import com.ammaia_ispc.sangreyamobile.screens.RegisterScreen;
import com.ammaia_ispc.sangreyamobile.support.NetworkIdlingResource;
import com.ammaia_ispc.sangreyamobile.support.TestFixtures;
import com.ammaia_ispc.sangreyamobile.support.TestFixtures.Account;
import com.ammaia_ispc.sangreyamobile.support.TestFixtures.CampaignData;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onIdle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public final class CriticalFlowsTest {
    private final NetworkIdlingResource network = new NetworkIdlingResource();
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private TestFixtures fixtures;
    private ActivityScenario<LoginActivity> scenario;

    @Before
    public void setUp() throws Exception {
        IdlingPolicies.setIdlingResourceTimeout(45, TimeUnit.SECONDS);
        IdlingPolicies.setMasterPolicyTimeout(60, TimeUnit.SECONDS);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            SessionManager.clearSession(context);
            ApiClient.setTestCallAdapterFactory(network);
        });
        IdlingRegistry.getInstance().register(network);
        fixtures = new TestFixtures();
    }

    @After
    public void tearDown() throws Exception {
        try {
            onIdle();
        } finally {
            if (scenario != null) {
                scenario.close();
            }
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                for (Stage stage : Stage.values()) {
                    if (stage != Stage.DESTROYED) {
                        for (Activity activity : new ArrayList<>(ActivityLifecycleMonitorRegistry
                                .getInstance().getActivitiesInStage(stage))) {
                            activity.finish();
                        }
                    }
                }
                SessionManager.clearSession(context);
                ApiClient.setTestCallAdapterFactory(null);
            });
            InstrumentationRegistry.getInstrumentation().getUiAutomation()
                    .setOnAccessibilityEventListener(null);
            IdlingRegistry.getInstance().unregister(network);
            if (fixtures != null) {
                fixtures.close();
            }
        }
    }

    @Test
    public void tcUI01_registerAndLogin() throws Exception {
        // Arrange
        Account account = fixtures.newAccount("UI01");
        scenario = ActivityScenario.launch(LoginActivity.class);
        LoginScreen login = new LoginScreen();

        // Act
        // Assert: intermediate checkpoints belong to the same registration/login flow.
        assertTrue("TC-UI-01: Login inicial", login.isOpen());
        RegisterScreen register = login.openRegistration();
        register.submit();
        assertNotNull("TC-UI-01: aviso de datos obligatorios", register.nameError());
        assertTrue("TC-UI-01: registro inválido no abandona el formulario", register.isOpen());
        assertEquals("TC-UI-01: registro inválido no crea cuenta", 0, fixtures.accountCount(account));
        passed("TC-UI-01", "Validación bloquea registro vacío");

        register.complete(account);
        register.submit();
        assertTrue("TC-UI-01: formulario limpio después del alta", register.isFormCleared());
        assertEquals("TC-UI-01: se crea exactamente una cuenta", 1, fixtures.accountCount(account));
        passed("TC-UI-01", "Registro válido: una cuenta creada");

        login = register.returnToLogin();
        assertTrue("TC-UI-01: retorno a Login", login.isOpen());
        CampaignListScreen campaigns = login.signIn(account);

        // Assert
        assertTrue("TC-UI-01: Login abre el área estándar", campaigns.isStandardAreaOpen());
        assertTrue("TC-UI-01: sesión estándar", SessionManager.isStandardUser(context));
        passed("TC-UI-01", "Retorno a Login y acceso con la cuenta registrada");
    }

    @Test
    public void tcUI02_adminCampaignCrud() throws Exception {
        // Arrange
        CampaignData original = fixtures.newCampaign("UI02");
        CampaignData edited = fixtures.editedCampaign(original);
        scenario = ActivityScenario.launch(LoginActivity.class);

        // Act: create -> read -> update -> read -> cancel -> delete.
        // Assert: verify each resulting state before continuing the flow.
        AdminDashboardScreen dashboard = new LoginScreen()
                .signInAsAdmin(fixtures.adminEmail, fixtures.adminPassword);
        assertTrue("TC-UI-02: panel administrador", dashboard.isOpen());
        AdminCampaignListScreen list = dashboard.openCampaigns();
        CampaignFormScreen form = list.openCreation();
        form.complete(original);
        list = form.save();
        list.search(original.title);
        assertTrue("TC-UI-02: campaña creada en lista", list.contains(original.title));
        int id = fixtures.campaignNamed(original.title).getInt("id");
        CampaignDetailScreen detail = list.openDetail(original.title);
        assertCampaignDetail("TC-UI-02: creación", original, detail);
        detail.returnToList();
        passed("TC-UI-02", "Crear y consultar campaña propia");

        form = list.openEdit(original.title);
        form.complete(edited);
        list = form.save();
        list.search(edited.title);
        detail = list.openDetail(edited.title);
        assertCampaignDetail("TC-UI-02: edición persistida", edited, detail);
        assertCampaignStored(edited, fixtures.campaign(id));
        detail.returnToList();
        passed("TC-UI-02", "Editar y comprobar persistencia al reabrir");

        list.requestDeletion(edited.title);
        list.cancelDeletion();
        detail = list.openDetail(edited.title);
        assertCampaignDetail("TC-UI-02: cancelar baja desde lista", edited, detail);
        detail.returnToList();
        form = list.openEdit(edited.title);
        form.requestDeletion();
        form.cancelDeletion();
        list = form.returnToList();
        detail = list.openDetail(edited.title);
        assertCampaignDetail("TC-UI-02: cancelar baja desde formulario", edited, detail);
        assertCampaignStored(edited, fixtures.campaign(id));
        detail.returnToList();
        passed("TC-UI-02", "Cancelar ambos diálogos conserva la campaña");

        list.requestDeletion(edited.title);
        list.confirmDeletion();

        // Assert
        assertFalse("TC-UI-02: baja elimina la entrada de lista", list.contains(edited.title));
        assertEquals("TC-UI-02: detalle eliminado no disponible", 404, fixtures.campaignStatus(id));
        passed("TC-UI-02", "Eliminar: ausente en lista y detalle HTTP 404");
    }

    @Test
    public void tcUI03_consultAndEnroll() throws Exception {
        // Arrange
        Account account = fixtures.seedStandardAccount("UI03");
        CampaignData data = fixtures.newCampaign("UI03");
        int id = fixtures.seedCampaign(data);
        int initial = fixtures.campaign(id).getInt("total_inscriptos");
        scenario = ActivityScenario.launch(LoginActivity.class);

        // Act: query and enroll through the actual UI.
        // Assert: verify the detail and enrollment checkpoints as the flow progresses.
        CampaignListScreen list = new LoginScreen().signIn(account);
        assertTrue("TC-UI-03: área estándar", list.isStandardAreaOpen());
        CampaignDetailScreen detail = list.openCampaign(data.title);
        assertCampaignDetail("TC-UI-03: consulta", data, detail);
        assertEquals("TC-UI-03: contador inicial", initial, detail.registeredCount());
        passed("TC-UI-03", "Consultar lista y detalle de campaña disponible");

        detail.enroll();
        assertTrue("TC-UI-03: confirmación visible", detail.receivedMessage(
                context.getString(R.string.enrollment_success, initial + 1)));
        assertEquals("TC-UI-03: contador aumenta exactamente uno", initial + 1, detail.registeredCount());
        detail.returnToList();
        MyEnrollmentsScreen enrollments = list.openMyEnrollments();
        assertEquals("TC-UI-03: una entrada en Mis inscripciones", 1, enrollments.entriesFor(data.title));
        passed("TC-UI-03", "Inscripción confirmada, contador +1 y una entrada propia");

        detail = enrollments.openCampaign(data.title);
        detail.enroll();
        assertTrue("TC-UI-03: mensaje de duplicado", detail.receivedMessage(
                context.getString(R.string.enrollment_duplicate_error)));
        assertEquals("TC-UI-03: duplicado no incrementa contador", initial + 1, detail.registeredCount());
        detail.returnToList();
        list = enrollments.returnToCampaigns();
        enrollments = list.openMyEnrollments();

        // Assert
        assertEquals("TC-UI-03: sigue existiendo una sola entrada tras recargar", 1,
                enrollments.entriesFor(data.title));
        assertEquals("TC-UI-03: contador persistido sin duplicados", initial + 1,
                fixtures.campaign(id).getInt("total_inscriptos"));
        passed("TC-UI-03", "Segundo intento rechazado sin duplicar inscripción");
    }

    private void assertCampaignDetail(String step, CampaignData data, CampaignDetailScreen detail) {
        assertEquals(step + ": título", data.title, detail.title());
        assertEquals(step + ": ubicación", data.location, detail.location());
        assertEquals(step + ": descripción", data.description, detail.description());
        assertEquals(step + ": cupo", context.getString(R.string.maximum_capacity, data.capacity), detail.capacity());
        assertEquals(step + ": fechas", CampaignHelper.formatLongDate(data.startDate, data.endDate), detail.dates());
    }

    private void assertCampaignStored(CampaignData data, JSONObject stored) throws Exception {
        assertEquals("TC-UI-02: título en backend", data.title, stored.getString("titulo"));
        assertEquals("TC-UI-02: ubicación en backend", data.location, stored.getString("ubicacion"));
        assertEquals("TC-UI-02: descripción en backend", data.description, stored.getString("descripcion"));
        assertEquals("TC-UI-02: cupo en backend", data.capacity, stored.getInt("cupo_maximo"));
        assertEquals("TC-UI-02: inicio en backend", data.startDate, stored.getString("fecha_inicio"));
        assertEquals("TC-UI-02: fin en backend", data.endDate, stored.getString("fecha_fin"));
    }

    private static void passed(String caseId, String step) {
        Log.i("CriticalFlows", caseId + " | OK | " + step);
    }
}
