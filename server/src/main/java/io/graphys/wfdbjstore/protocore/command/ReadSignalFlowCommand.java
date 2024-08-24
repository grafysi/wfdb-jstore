package io.graphys.wfdbjstore.protocore.command;

import io.graphys.wfdbjstore.engine.SignalQuery;
import io.graphys.wfdbjstore.protocol.content.ReadSignalFlowContent;
import io.graphys.wfdbjstore.protocol.description.ReadSignalFlowDescription;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocore.report.ReportConsumer;

public final class ReadSignalFlowCommand
        extends Command<ReadSignalFlowDescription, ReadSignalFlowContent>
        implements SignalQuery {

    public ReadSignalFlowCommand(CommandType commandType, ReadSignalFlowDescription description, ReportConsumer consumer) {
        super(commandType, description, consumer);
    }

    @Override
    public void doAndWriteReport() throws Exception {
        var signalInput = getSignalInput();

        if (description.frameSkip() != null && description.frameSkip() != 0) {
            signalInput.seek(
                    Math.min(signalInput.getTotalSamples(),
                            signalInput.getSampleNumber() + description.frameSkip()));
        }

        for (int i = 0; i < description.frameLimit(); i++) {
            var sampleNumber = signalInput.getSampleNumber();
            var samples = signalInput.readSamples();
            var content = ReadSignalFlowContent.builder()
                    .sampleNumber(sampleNumber)
                    .samples(samples)
                    .build();
            writeContent(content);
        }

    }
}































