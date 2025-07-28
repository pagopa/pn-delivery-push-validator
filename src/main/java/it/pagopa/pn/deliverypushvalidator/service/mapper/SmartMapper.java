package it.pagopa.pn.deliverypushvalidator.service.mapper;

import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.ElementTimestampTimelineElementDetails;
import it.pagopa.pn.deliverypushvalidator.utils.FeatureEnabledUtils;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Set;
import java.util.function.BiFunction;

@Slf4j
@Component
public class SmartMapper {

    private FeatureEnabledUtils featureEnabledUtils;
    private final TimelineMapperFactory timelineMapperFactory;
    private static ModelMapper modelMapper;
    private static BiFunction postMappingTransformer;
    
    public SmartMapper(TimelineMapperFactory timelineMapperFactory, FeatureEnabledUtils featureEnabledUtils){
        this.timelineMapperFactory = timelineMapperFactory;
        this.featureEnabledUtils = featureEnabledUtils;
    }

    private static String SERCQ_SEND = "send-self";

    static Converter<TimelineElementInternal, TimelineElementInternal> timelineElementInternalTimestampConverter =
            ctx -> {
                // se il detail estende l'interfaccia e l'elementTimestamp non è nullo, lo sovrascrivo nel source originale
                if (ctx.getSource().getDetails() instanceof ElementTimestampTimelineElementDetails elementTimestampTimelineElementDetails
                    && elementTimestampTimelineElementDetails.getElementTimestamp() != null)
                {
                    return ctx.getSource().toBuilder()
                            .timestamp(elementTimestampTimelineElementDetails.getElementTimestamp())
                            .build();
                }

                return ctx.getSource();
            };

    /*
        Mapping effettuato per la modifica dei timestamp per gli
        elementi di timeline che implementano l'interfaccia ElementTimestampTimelineElementDetails
     */
    public static  <S,T> T mapToClass(S source, Class<T> destinationClass ){
        T result;
        if( source != null) {
            result = modelMapper.map(source, destinationClass );

            result = (T) postMappingTransformer.apply(source, result);
        } else {
            result = null;
        }
        return result;
    }

    /*
        Mapping effettuato per la modifica dei timestamp per gli
        elementi di timeline che implementano l'interfaccia ElementTimestampTimelineElementDetails
     */
    private static  TimelineElementInternal mapTimelineInternal(TimelineElementInternal source ){
        TimelineElementInternal result;
        if( source != null) {
            TimelineElementInternal elementToMap = source.toBuilder().build();
            result = modelMapper.map(elementToMap, TimelineElementInternal.class );
            result = (TimelineElementInternal) postMappingTransformer.apply(source, result);
        } else {
            result = null;
        }
        return result;
    }

    /**
        Remapping per gli elementi di timeline per il workflow analogico (workaround per PN-9059)
        I restanti remapping vengono gestiti tramite il typeMap e l'interfaccia ElementTimestampTimelineElementDetails
     */
    public TimelineElementInternal mapTimelineInternal(TimelineElementInternal source, Set<TimelineElementInternal> timelineElementInternalSet) {
        //Viene recuperato il timestamp originale, prima di effettuare un qualsiasi remapping
        Instant ingestionTimestamp = source.getTimestamp();

        //Viene effettuato un primo remapping degli elementi di timeline e dei relativi timestamp in particolare viene effettuato il remapping di tutti 
        // i timestamp che non dipendono da ulteriori elementi di timeline, cioè hanno l'eventTimestamp già storicizzato nei details
        TimelineElementInternal result = mapTimelineInternal(source);

        TimelineMapper timelineMapper = timelineMapperFactory.getTimelineMapper(source.getNotificationSentAt());
        boolean isPfNewWorkflowEnabled = featureEnabledUtils.isPfNewWorkflowEnabled(source.getNotificationSentAt());
        timelineMapper.remapSpecificTimelineElementData(timelineElementInternalSet, result, ingestionTimestamp, isPfNewWorkflowEnabled);

        return result;
    }

}
