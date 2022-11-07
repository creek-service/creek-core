
import org.creekservice.api.service.extension.CreekExtensionProvider;

/** Defines types used by Creek Service extensions */
module creek.service.extension {
    requires transitive creek.platform.metadata;

    exports org.creekservice.api.service.extension;
    exports org.creekservice.api.service.extension.component;
    exports org.creekservice.api.service.extension.component.model;
    exports org.creekservice.api.service.extension.option;
    exports org.creekservice.api.service.extension.extension;

    uses CreekExtensionProvider;
}
