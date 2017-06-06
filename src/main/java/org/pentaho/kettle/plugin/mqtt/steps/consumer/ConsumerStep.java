/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
*
*******************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/

package org.pentaho.kettle.plugin.mqtt.steps.consumer;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

import java.util.List;

/**
 * Created by dams on 02-08-2016.
 */
public class ConsumerStep extends BaseStep implements StepInterface {

    private static final Class<?> PKG = ConsumerStep.class;
    
    MqttClientSubscriber client = null;

    /**
     * The constructor should simply pass on its arguments to the parent class.
     *
     * @param s                 step description
     * @param stepDataInterface step data class
     * @param c                 step copy
     * @param t                 transformation description
     * @param dis               transformation executing
     */
    public ConsumerStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
        super(s, stepDataInterface, c, t, dis);
    }

    /**
     * This method is called by PDI during transformation startup.
     * <p>
     * It should initialize required for step execution.
     * <p>
     * The meta and data implementations passed in can safely be cast
     * to the step's respective implementations.
     * <p>
     * It is mandatory that super.init() is called to ensure correct behavior.
     * <p>
     * Typical tasks executed here are establishing the connection to a database,
     * as wall as obtaining resources, like file handles.
     *
     * @param smi step meta interface implementation, containing the step settings
     * @param sdi step data interface implementation, used to store runtime information
     * @return true if initialization completed successfully, false if there was an error preventing the step from working.
     */

    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        ConsumerStepMeta meta = (ConsumerStepMeta) smi;
        ConsumerStepData data = (ConsumerStepData) sdi;

        if (super.init(meta, data)) {
            data.readsRows = getStepMeta().getRemoteInputSteps().size() > 0;
            List<StepMeta> previous = getTransMeta().findPreviousSteps(getStepMeta());
            if (previous != null && previous.size() > 0) {
                data.readsRows = true;
            }
            return true;
        }
        return false;
    }

    /**
     * Once the transformation starts executing, the processRow() method is called repeatedly
     * by PDI for as long as it returns true. To indicate that a step has finished processing rows
     * this method must call setOutputDone() and return false;
     * <p>
     * Steps which process incoming rows typically call getRow() to read a single row from the
     * input stream, change or add row content, call putRow() to pass the changed row on
     * and return true. If getRow() returns null, no more rows are expected to come in,
     * and the processRow() implementation calls setOutputDone() and returns false to
     * indicate that it is done too.
     * <p>
     * Steps which generate rows typically construct a new row Object[] using a call to
     * RowDataUtil.allocateRowData(numberOfFields), add row content, and call putRow() to
     * pass the new row on. Above process may happen in a loop to generate multiple rows,
     * at the end of which processRow() would call setOutputDone() and return false;
     *
     * @param smi the step meta interface containing the step settings
     * @param sdi the step data interface that should be used to store
     * @return true to indicate that the function should be called again, false if the step is done
     */
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

        // safely cast the step settings (meta) and runtime info (data) to specific implementations
        ConsumerStepMeta meta = (ConsumerStepMeta) smi;
        ConsumerStepData data = (ConsumerStepData) sdi;
        
        if (first) {
        	Object[] r = new Object[]{}; //empty row
            first = false;
            data.outputRowMeta = new RowMeta();
            // use meta.getFields() to change it, so it reflects the output row structure
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this, null, null);
            client = new MqttClientSubscriber(meta, data, this, r);
            client.start();
        }
        
        try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return true;
    }


    /**
     * This method is called by PDI once the step is done processing.
     * <p>
     * The dispose() method is the counterpart to init() and should release any resources
     * acquired for step execution like file handles or database connections.
     * <p>
     * The meta and data implementations passed in can safely be cast
     * to the step's respective implementations.
     * <p>
     * It is mandatory that super.dispose() is called to ensure correct behavior.
     *
     * @param smi step meta interface implementation, containing the step settings
     * @param sdi step data interface implementation, used to store runtime information
     */
    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
        ConsumerStepMeta meta = (ConsumerStepMeta) smi;
        ConsumerStepData data = (ConsumerStepData) sdi;

        super.dispose(meta, data);
        if(client != null)
        	client.stop();
    }
}
